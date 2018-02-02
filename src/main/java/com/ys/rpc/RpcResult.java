package com.ys.rpc;

import com.ys.common.Result;

/**
 * <p>@description:</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/2 11:38
 */
public class RpcResult implements Result {




    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public boolean hasException() {
        return false;
    }
}
