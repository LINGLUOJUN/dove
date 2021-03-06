package com.six.dove.remote.server;

import java.lang.reflect.Method;

import com.six.dove.remote.Remote;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.ServicePath;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.util.ClassUtils;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public interface ServerRemote extends Remote<RemoteRequest, Void, RemoteResponse, Void, ServerRemoteConnection> {

	/**
	 * 远程调用服务端服务默认版本
	 */
	public static final int DEFAULT_SERVICE_VERSION = 1;

	/**
	 * 构建远程调用服务网服务命名
	 * 
	 * @param fullClassName
	 *            协议class全名
	 * @param protocolMethod
	 *            协议方法
	 * @param version
	 *            协议版本号
	 * @return
	 */
	public static ServiceName newServiceName(String fullClassName, Method protocolMethod, int version) {
		return ServiceName.newServiceName(fullClassName, protocolMethod.getName(),
				ClassUtils.parmaTypes(protocolMethod), version);
	}

	/**
	 * 服务端host
	 * 
	 * @return 一定有值
	 */
	String getLocalHost();

	/**
	 * 服务端监听端口
	 * 
	 * @return 一定有值
	 */
	int getListenPort();

	/**
	 * 生成一个ServicePath
	 * 
	 * @param serviceName
	 *            服务命名
	 * @return
	 */
	ServicePath newServicePath(ServiceName serviceName);

	/**
	 * 生成远程调用服务端代理类class name
	 * 
	 * @param protocol
	 *            协议class
	 * @param instanceMethod
	 *            协议class调用方法
	 * @return
	 */
	String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod);

	/**
	 * 生成远程调用客户端代理类class code
	 * 
	 * @param protocolClass
	 *            协议class
	 * @param packageName
	 *            协议class所在包
	 * @param className
	 *            协议class 代理名称
	 * @param instanceMethod
	 *            协议class 调用方法
	 * @return
	 */
	String generateProtocolProxyClassCode(Class<?> protocolClass, String packageName, String className,
			Method instanceMethod);

	/**
	 * 获取服务端包装的调用服务
	 * 
	 * @param serviceName
	 *            调用服务命名
	 * @return 返回可调用的服务，有可能为Null
	 */
	WrapperServiceTuple getWrapperServiceTuple(ServiceName serviceName);

	/**
	 * 注册远程调用服务端的代理服务
	 * 
	 * @param serviceName
	 *            服务命名
	 * @param wrapperServiceTuple
	 *            代理服务包装
	 */
	void registerWrapperServiceTuple(ServiceName serviceName, WrapperServiceTuple wrapperServiceTuple);

	/**
	 * 移除远程调用服务端的代理服务网
	 * 
	 * @param serviceName
	 *            服务命名
	 */
	void removeWrapperServiceTuple(ServiceName serviceName);

}
