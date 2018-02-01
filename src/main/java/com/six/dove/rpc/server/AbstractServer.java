package com.six.dove.rpc.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.ServiceHook;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.ServicePath;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.protocol.RemoteSerialize;
import com.six.dove.remote.server.AbstractServerRemote;
import com.six.dove.remote.server.WrapperService;
import com.six.dove.remote.server.WrapperServiceTuple;
import com.six.dove.rpc.RpcServer;
import com.six.dove.rpc.register.LocalRpcRegister;
import com.six.dove.rpc.register.RpcRegister;
import com.six.dove.util.ClassUtils;

import io.netty.util.NettyRuntime;

/**
 * @author sixliu
 * @date 2017年12月29日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractServer extends AbstractServerRemote implements RpcServer {

	final static Logger log = LoggerFactory.getLogger(AbstractServer.class);
	public static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, NettyRuntime.availableProcessors() * 2);
	private Map<ServiceName, WrapperServiceTuple> registerMap = new ConcurrentHashMap<>();
	private ExecutorService defaultBizExecutorService;
	private RpcRegister rpcRegister;

	public AbstractServer(String name,String localHost, int listenPort, Compiler compiler, RemoteSerialize remoteSerialize) {
		super(name,localHost, listenPort, compiler, remoteSerialize);
		defaultBizExecutorService = Executors.newFixedThreadPool(DEFAULT_EVENT_LOOP_THREADS, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "NettyRpcServer-worker-biz-thread_" + this.threadIndex.incrementAndGet());
			}
		});
		rpcRegister = new LocalRpcRegister();
	}

	@Override
	public final WrapperServiceTuple getWrapperServiceTuple(ServiceName serviceName) {
		return registerMap.get(serviceName);
	}

	@Override
	public <T, I extends T> void register(Class<T> protocol, I instance) {
		register(defaultBizExecutorService, protocol, instance);
	}

	@Override
	public <T, I extends T> void register(Class<T> protocol, I instance, ServiceHook hook) {
		register(defaultBizExecutorService, protocol, instance, null == hook ? ServiceHook.DEFAULT_HOOK : hook);
	}

	@Override
	public <T, I extends T> void register(ExecutorService bizExecutorService, Class<T> protocol, I instance) {
		register(bizExecutorService, protocol, instance, ServiceHook.DEFAULT_HOOK);
	}

	@Override
	public <T, I extends T> void register(ExecutorService bizExecutorService, Class<T> protocol, I instance,
			ServiceHook hook) {
		Objects.requireNonNull(bizExecutorService, "bizExecutorService must not be null");
		Objects.requireNonNull(protocol, "protocol must not be null");
		Objects.requireNonNull(instance, "instance must not be null");
		Objects.requireNonNull(hook, "hook must not be null");
		if (!protocol.isAssignableFrom(instance.getClass())) {
			throw new RuntimeException("protocolClass " + protocol.getCanonicalName()
					+ " is not implemented by protocolImpl which is of class "
					+ instance.getClass().getCanonicalName());
		}
		int modifiers = instance.getClass().getModifiers();
		if (!"public".equals(Modifier.toString(modifiers))) {
			throw new RuntimeException("the instance's class[" + instance.getClass().getCanonicalName()
					+ "] is not public protocolClass ");
		}
		String fullProtocolClassName = protocol.getCanonicalName();
		Method[] protocolMethods = protocol.getMethods();
		String packageName = instance.getClass().getPackage().getName();
		WrapperService wrapperService = null;
		ServicePath servicePath = null;
		ServiceName serviceName = null;
		for (Method protocolMethod : protocolMethods) {
			serviceName = newServiceName(fullProtocolClassName, protocolMethod, DEFAULT_SERVICE_VERSION);
			String proxyClassName = generateProtocolProxyClassName(protocol, protocolMethod);
			String fullProxyClassName = packageName + "." + proxyClassName;
			wrapperService = (WrapperService) getCompiler().findOrCompile(fullProxyClassName,
					new Class<?>[] { protocol }, new Object[] { instance }, () -> {
						return buildServerWrapperServiceCode(protocol, packageName, proxyClassName, protocolMethod);
					});
			registerMap.put(serviceName, new WrapperServiceTuple(wrapperService, bizExecutorService, hook));
			servicePath = ServicePath.newServicePath(getLocalHost(), getListenPort(), serviceName);
			rpcRegister.deploy(serviceName, servicePath);
		}
	}

	private static ServiceName newServiceName(String fullClassName, Method protocolMethod, int version) {
		return ServiceName.newServiceName(fullClassName, protocolMethod.getName(),
				ClassUtils.parmaTypes(protocolMethod), version);
	}

	@Override
	public void unregister(Class<?> protocol) {
		Objects.requireNonNull(protocol, "protocol must not be null");
		Method[] protocolMethods = protocol.getMethods();
		String fullProtocolClassName = protocol.getCanonicalName();
		for (Method protocolMethod : protocolMethods) {
			registerMap.remove(newServiceName(fullProtocolClassName, protocolMethod, DEFAULT_SERVICE_VERSION));
		}
	}

	protected ExecutorService getDefaultBizExecutorService() {
		return defaultBizExecutorService;
	}

	@Override
	protected final void shutdown() {
		destroy();
		registerMap.forEach((key, value) -> {
			try {
				rpcRegister.undeploy(key);
			} catch (Exception e) {
				log.error("undeploy service[" + key + "] exception", e);
			}
		});
		registerMap.clear();
	}

	protected abstract void destroy();
}