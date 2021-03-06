package com.six.dove.remote.server;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.AbstractRemote;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.ServicePath;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.compiler.impl.JavaCompilerImpl;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.protocol.RemoteResponseState;
import com.six.dove.remote.protocol.RemoteSerialize;
import com.six.dove.util.ClassUtils;
import com.six.dove.util.ExceptionUtils;

import io.netty.util.NettyRuntime;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 抽象的服务调用端类
 */
public abstract class AbstractServerRemote extends
		AbstractRemote<RemoteRequest, Void, RemoteResponse, Void, ServerRemoteConnection> implements ServerRemote {

	final static Logger log = LoggerFactory.getLogger(AbstractServerRemote.class);

	public static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, NettyRuntime.availableProcessors() * 2);
	private String localHost;
	private int listenPort;
	private Map<ServiceName, WrapperServiceTuple> registerMap;


	public AbstractServerRemote(String name, String localHost, int listenPort) {
		this(name, localHost, listenPort, new JavaCompilerImpl(), new RemoteSerialize() {
		});
	}

	public AbstractServerRemote(String name, String localHost, int listenPort, Compiler compiler,
			RemoteSerialize rpcSerialize) {
		super(name, compiler, rpcSerialize);
		this.localHost = localHost;
		this.listenPort = listenPort;
		this.registerMap = new ConcurrentHashMap<>();
	}

	@Override
	public final WrapperServiceTuple getWrapperServiceTuple(ServiceName serviceName) {
		return registerMap.get(serviceName);
	}

	@Override
	public final void registerWrapperServiceTuple(ServiceName serviceName, WrapperServiceTuple wrapperServiceTuple) {
		registerMap.put(serviceName, wrapperServiceTuple);
	}
	
	@Override
	public void removeWrapperServiceTuple(ServiceName serviceName) {
		registerMap.remove(serviceName);
	}
	
	@Override
	public final Void execute(RemoteRequest rpcRequest) {
		RemoteResponse rpcResponse = new RemoteResponse();
		rpcResponse.setId(rpcRequest.getId());
		WrapperServiceTuple wrapperServiceTuple = getWrapperServiceTuple(rpcRequest.getServiceName());
		String address = rpcRequest.getServerRpcConnection().toString();
		if (null != wrapperServiceTuple) {
			try {
				wrapperServiceTuple.getExecutorService().submit(() -> {
					log.debug("server received coommand[" + rpcRequest.getServiceName() + "] from:" + address);
					try {
						wrapperServiceTuple.getHook().beforeHook(rpcRequest.getParams());
						Object result = wrapperServiceTuple.getWrapperService().invoke(rpcRequest.getParams());
						wrapperServiceTuple.getHook().afterHook(rpcRequest.getParams());
						rpcResponse.setStatus(RemoteResponseState.SUCCEED);
						rpcResponse.setResult(result);
					} catch (Exception e) {
						wrapperServiceTuple.getHook().exceptionHook(rpcRequest.getParams());
						String errMsg = ExceptionUtils.getExceptionMsg(e);
						rpcResponse.setStatus(RemoteResponseState.INVOKE_ERR);
						rpcResponse.setMsg(errMsg);
						log.error("invoke request[" + address + "] err", e);
					}
					rpcRequest.getServerRpcConnection().send(rpcResponse);
				});
			} catch (RejectedExecutionException e) {
				// 业务处理线程池满了，拒绝异常
				rpcResponse.setStatus(RemoteResponseState.REJECT);
				String msg = "the service is too busy and reject rpcRequest[" + address + "]:"
						+ rpcRequest.getServiceName();
				rpcResponse.setMsg(msg);
				log.error(msg);
				rpcRequest.getServerRpcConnection().send(rpcResponse);
			}
		} else {
			rpcResponse.setStatus(RemoteResponseState.UNFOUND_SERVICE);
			String msg = "unfound service by rpcRequest[" + address + "]:" + rpcRequest.getServiceName();
			rpcResponse.setMsg(msg);
			log.error(msg);
			rpcRequest.getServerRpcConnection().send(rpcResponse);
		}
		return null;
	}

	public ServicePath newServicePath(ServiceName serviceName) {
		ServicePath servicePath = new ServicePath();
		servicePath.setHost(localHost);
		servicePath.setPort(listenPort);
		servicePath.setServiceName(serviceName);
		return servicePath;
	}

	@Override
	public String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = protocol.getSimpleName();
		String instanceMethodName = instanceMethod.getName();
		classSb.append("RpcServerServiceProxy$");
		classSb.append(instanceName).append("$");
		classSb.append(instanceMethodName);
		Parameter[] parameter = instanceMethod.getParameters();
		if (null != parameter && parameter.length > 0) {
			classSb.append("$");
			String parameterTypeName = null;
			StringBuilder invokePamasSb = new StringBuilder();
			for (int i = 0, size = parameter.length; i < size; i++) {
				parameterTypeName = parameter[i].getParameterizedType().getTypeName();
				parameterTypeName = parameterTypeName.replace(".", "_");
				invokePamasSb.append(parameterTypeName).append("$");
			}
			invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
			classSb.append(invokePamasSb);
		}
		return classSb.toString();
	}

	@Override
	public String generateProtocolProxyClassCode(Class<?> protocolClass, String packageName, String className,
			Method instanceMethod) {
		String method = instanceMethod.getName();
		String instanceType = protocolClass.getCanonicalName();
		Parameter[] parameter = instanceMethod.getParameters();
		StringBuilder clz = new StringBuilder();
		clz.append("package ").append(packageName).append(";\n");
		clz.append("import ").append(WrapperService.class.getName()).append(";\n");
		clz.append("public class ").append(className)
				.append(" implements " + WrapperService.class.getSimpleName() + " {\n");
		clz.append("	private ").append(instanceType).append(" instance;\n");
		clz.append("	public " + className + "(").append(instanceType).append(" instance").append("){\n");
		clz.append("		this.instance=instance;\n");
		clz.append("	}\n");
		clz.append("	@Override\n");
		clz.append("	public Object invoke(Object[] paras)throws Exception{\n");
		String invokePamasStr = "";
		if (null != parameter && parameter.length > 0) {
			String parameterTypeName = null;
			StringBuilder invokePamasSb = new StringBuilder();
			for (int i = 0, size = parameter.length; i < size; i++) {
				parameterTypeName = parameter[i].getParameterizedType().getTypeName();
				invokePamasSb.append("(").append(parameterTypeName).append(")").append("paras[").append(i).append("],");
			}
			invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
			invokePamasStr = invokePamasSb.toString();
		}
		if (ClassUtils.hasReturnType(instanceMethod)) {
			clz.append("		return this.instance.").append(method);
			clz.append("(").append(invokePamasStr).append(");\n");
		} else {
			clz.append("		this.instance.").append(method);
			clz.append("(").append(invokePamasStr).append(");\n");
			clz.append("		return null;\n");
		}
		clz.append("	}\n");
		clz.append("}\n");
		return clz.toString();
	}

	@Override
	public String getLocalHost() {
		return localHost;
	}

	@Override
	public int getListenPort() {
		return listenPort;
	}
	
	@Override
	protected final void handlerStop() {
		registerMap.clear();
	}

	protected abstract void stop2();
}
