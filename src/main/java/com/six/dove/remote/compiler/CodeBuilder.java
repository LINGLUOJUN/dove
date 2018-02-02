package com.six.dove.remote.compiler;

/**
 * <p>@description:代码构建器</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/2 13:31
 */
@FunctionalInterface
public interface CodeBuilder {

    /**
     * 构建
     *
     * @return
     */
    String build();
}
