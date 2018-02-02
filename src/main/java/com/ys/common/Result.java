package com.ys.common;

/**
 * <p>@description:返回结果(消费端和服务端都使用)</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/2 11:36
 */
public interface Result {


    Object getValue();

    /**
     * Get exception.
     *
     * @return exception. if no exception return null.
     */
    Throwable getException();

    /**
     * Has exception.
     *
     * @return has exception.
     */
    boolean hasException();
}
