package com.ys.rpc;

import com.ys.common.Invocation;
import com.ys.common.Result;

/**
 * <p>@description:</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/2 14:44
 */
public class ConsumerFilter implements Filter{
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return null;
    }
}
