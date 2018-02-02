package com.ys.rpc;

/**
 * <p>@description:</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/2 14:59
 */
public interface Node {

    /**
     * get url.
     *
     * @return url.
     */
//    URL getUrl();

    /**
     * is available.
     *
     * @return available.
     */
    boolean isAvailable();

    /**
     * destroy.
     */
    void destroy();
}
