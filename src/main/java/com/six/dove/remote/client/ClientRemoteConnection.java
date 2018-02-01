package com.six.dove.remote.client;

import com.six.dove.remote.connection.RemoteConnection;
import com.six.dove.remote.protocol.RemoteRequest;

/**
 * @author yangshuang
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 客户端连接
 */
public interface ClientRemoteConnection extends RemoteConnection<RemoteRequest, RemoteFuture> {


    /**
     * 添加远程回调
     *
     * @param rpcRequestId
     * @param remoteFuture
     */
    void putRemoteFuture(String rpcRequestId, RemoteFuture remoteFuture);

    /**
     * 当客户端消费时移除远程回调
     *
     * @param rpcRequestId
     * @return
     */
    RemoteFuture removeRemoteFuture(String rpcRequestId);

}
