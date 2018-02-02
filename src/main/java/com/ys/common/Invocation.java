package com.ys.common;


/**
 * 服务调用单元
 * @author yangshuang
 * @version 1.0
 */
public interface Invocation {



    /**
     * 获取服务名称
     *
     * @return
     */
    String getName();

    /**
     * 获取服务状态
     *
     * @return
     */
    State getState();


    /**
     * 服务是否正在运行
     *
     * @return
     */
    boolean isRunning();

    /**
     * 启动服务
     */
    void start();

    /**
     * 停止服务
     */
    void stop();

}
