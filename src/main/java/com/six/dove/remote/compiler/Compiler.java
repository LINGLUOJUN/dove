package com.six.dove.remote.compiler;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public interface Compiler {


    /**
     * 获取
     *
     * @param parameterTypes
     * @param initArgs
     * @param codeBuilder    构造器
     * @param fullClassName
     * @return
     */
    Object findOrCompile(String fullClassName, Class<?>[] parameterTypes, Object[] initArgs, CodeBuilder codeBuilder);
}
