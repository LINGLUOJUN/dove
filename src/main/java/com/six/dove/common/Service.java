package com.six.dove.common;

import com.six.dove.common.support.State;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description 服务调用接口
 */
public interface Service {



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
