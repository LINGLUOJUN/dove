package com.six.dove.remote;

import java.io.Serializable;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public class ServiceName implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6963767724643942642L;

	/**
	 * 服务类名
	 */
	private String className;

	/**
	 * 服务方法
	 */
	private String methodName;

	/**
	 * 服务方法参数类型
	 */
	private String[] paramTypes;

	/**
	 * 服务版本
	 */
	private int version;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String[] getParamTypes() {
		return paramTypes;
	}

	public void setParamTypes(String[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object ob) {
		if (null != ob && ob instanceof ServiceName) {
			ServiceName obServiceName = (ServiceName) ob;
			return toString().equals(obServiceName.toString());
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(className).append("_");
		sb.append(methodName).append("_");
		if (null != paramTypes) {
			for (String parmaType : paramTypes) {
				sb.append(parmaType).append("_");
			}
		}
		sb.append(version);
		return sb.toString();
	}

	public static ServiceName newServiceName(String className, String methodName, String[] parmaTypes, int version) {
		ServiceName serviceName = new ServiceName();
		serviceName.setClassName(className);
		serviceName.setMethodName(methodName);
		serviceName.setParamTypes(parmaTypes);
		serviceName.setVersion(version);
		return serviceName;
	}
}
