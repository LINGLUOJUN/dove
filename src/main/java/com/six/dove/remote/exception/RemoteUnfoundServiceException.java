package com.six.dove.remote.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午9:01:34
 * 
 *       Rpc 没有找到服务异常
 */
public class RemoteUnfoundServiceException extends RemoteException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 45885420128158552L;

	public RemoteUnfoundServiceException(String message) {
		super(message);
	}

	public RemoteUnfoundServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
