package com.ys.rpc;


import com.ys.common.Invocation;
import com.ys.common.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * <p>@description:远程的服务调用单元</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/2 11:15
 */
public abstract class AbstractInvocation implements Invocation {

    private static Logger log = LoggerFactory.getLogger(AbstractInvocation.class);
    /**
     * 服务状态更新
     */
    private static AtomicReferenceFieldUpdater<AbstractInvocation, State> STATE_UPDATE = AtomicReferenceFieldUpdater
            .newUpdater(AbstractInvocation.class, State.class, "state");
    /**
     * 服务名称
     */
    private final String name;

    /**
     * 服务状态
     */
    private volatile State state = State.INIT;


    public AbstractInvocation(String name) {
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
        return State.START == state;
    }

    @Override
    public final void start() {
        if (STATE_UPDATE.compareAndSet(this, State.INIT, State.START)) {
            log.info("will start service[" + getName() + "]");
            doStart();
            log.info("started service[" + getName() + "]");
        }
    }


    /**
     * 启用服务
     */
    protected abstract void doStart();

    @Override
    public final void stop() {
        if (STATE_UPDATE.compareAndSet(this, State.START, State.STOP)) {
            log.info("will stop service[" + getName() + "]");
            doStop();
            log.info("stoped service[" + getName() + "]");
        }
    }

    /**
     * 停止无法
     */
    protected abstract void doStop();
}
