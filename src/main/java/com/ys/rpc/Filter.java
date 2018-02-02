package com.ys.rpc;

import com.ys.common.Invocation;
import com.ys.common.Result;

/**
 * <p>@description:</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/2 11:40
 */
public interface Filter {


    Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException;
}
