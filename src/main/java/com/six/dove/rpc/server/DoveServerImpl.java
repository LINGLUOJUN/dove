package com.six.dove.rpc.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.common.AbstractService;
import com.six.dove.remote.ServiceHook;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.ServicePath;
import com.six.dove.remote.server.ServerRemote;
import com.six.dove.remote.server.WrapperService;
import com.six.dove.remote.server.WrapperServiceTuple;
import com.six.dove.remote.server.netty.NettyServerRemote;
import com.six.dove.rpc.DoveServer;
import com.six.dove.rpc.register.DoveRegister;
import com.six.dove.rpc.register.LocalDoveRegister;

import io.netty.util.NettyRuntime;

/**
 * @author sixliu
 * @date 2017年12月29日
 * @email 359852326@qq.com
 * @Description
 */
public class DoveServerImpl extends AbstractService implements DoveServer {

	final static Logger log = LoggerFactory.getLogger(DoveServerImpl.class);
	public static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, NettyRuntime.availableProcessors() * 2);
	private ServerRemote serverRemote;
	private ExecutorService defaultBizExecutorService;
	private DoveRegister doveRegister;

	public DoveServerImpl(String localHost, int listenPort) {
		this(new NettyServerRemote(localHost, listenPort));
	}

	public DoveServerImpl(ServerRemote serverRemote) {
		this(serverRemote, new LocalDoveRegister(), newDefaultBizExecutorService());
	}

	public DoveServerImpl(ServerRemote serverRemote, DoveRegister doveRegister, ExecutorService defaultBizExecutorService) {
		super("dove-server");
		Objects.requireNonNull(serverRemote);
		Objects.requireNonNull(doveRegister);
		Objects.requireNonNull(defaultBizExecutorService);
		this.serverRemote = serverRemote;
		this.doveRegister = doveRegister;
		this.defaultBizExecutorService = defaultBizExecutorService;
	}

	private static ExecutorService newDefaultBizExecutorService() {
		return Executors.newFixedThreadPool(DEFAULT_EVENT_LOOP_THREADS, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "NettyRpcServer-worker-biz-thread_" + this.threadIndex.incrementAndGet());
			}
		});
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
			serviceName = ServerRemote.newServiceName(fullProtocolClassName, protocolMethod,
					ServerRemote.DEFAULT_SERVICE_VERSION);
			String proxyClassName = serverRemote.generateProtocolProxyClassName(protocol, protocolMethod);
			String fullProxyClassName = packageName + "." + proxyClassName;
			wrapperService = (WrapperService) serverRemote.getCompiler().compile(fullProxyClassName,
					new Class<?>[] { protocol }, new Object[] { instance }, () -> {
						return serverRemote.generateProtocolProxyClassCode(protocol, packageName, proxyClassName,
								protocolMethod);
					});
			serverRemote.registerWrapperServiceTuple(serviceName,
					new WrapperServiceTuple(wrapperService, bizExecutorService, hook));
			servicePath = serverRemote.newServicePath(serviceName);
			doveRegister.deploy(serviceName, servicePath);
		}
	}

	@Override
	public void unregister(Class<?> protocol) {
		Objects.requireNonNull(protocol, "protocol must not be null");
		Method[] protocolMethods = protocol.getMethods();
		String fullProtocolClassName = protocol.getCanonicalName();
		ServiceName serviceName = null;
		for (Method protocolMethod : protocolMethods) {
			ServerRemote.newServiceName(fullProtocolClassName, protocolMethod, ServerRemote.DEFAULT_SERVICE_VERSION);
			serverRemote.removeWrapperServiceTuple(ServerRemote.newServiceName(fullProtocolClassName, protocolMethod,
					ServerRemote.DEFAULT_SERVICE_VERSION));
			doveRegister.undeploy(serviceName);
		}
	}

	protected ExecutorService getDefaultBizExecutorService() {
		return defaultBizExecutorService;
	}

	@Override
	protected void doStart() {
		serverRemote.start();
		doveRegister.start();
	}

	@Override
	protected void doStop() {
		serverRemote.stop();
		doveRegister.stop();
	}
}
