package com.ys.rpc;

import com.six.dove.common.AbstractService;
import com.six.dove.common.Service;
import com.ys.common.Invocation;
import com.ys.common.State;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * <p>@description:远程的服务调用单元</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/2 11:15
 */
public class RpcInvocation implements Invocation {
    /**
     * 服务状态更新
     */
    private static AtomicReferenceFieldUpdater<AbstractService, Service.State> STATE_UPDATE = AtomicReferenceFieldUpdater
            .newUpdater(AbstractService.class, Service.State.class, "state");
    /**
     * 服务名称
     */
    private final String name;

    /**
     * 服务状态
     */
    private volatile State state = State.INIT;
    public RpcInvocation(String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }



    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final State getState() {
        return state;
    }

    @Override
    public final boolean isRunning() {
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
