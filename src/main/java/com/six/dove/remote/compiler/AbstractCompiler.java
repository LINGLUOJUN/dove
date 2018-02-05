package com.six.dove.remote.compiler;

import java.lang.reflect.Constructor;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public abstract class AbstractCompiler implements Compiler {

    @Override
    public final Object compile(String fullClassName, Class<?>[] parameterTypes, Object[] initArgs, CodeBuilder codeBuilder) {
        try {
            Class<?> clz = findClass(fullClassName, codeBuilder);
            Constructor<?> constructor = null;
            if (null == parameterTypes) {
                constructor = clz.getConstructor();
            } else {
                constructor = clz.getConstructor(parameterTypes);
            }
            if (null == initArgs) {
                return constructor.newInstance();
            } else {
                return constructor.newInstance(initArgs);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> findClass(String fullClassName, CodeBuilder codeBuilder) throws Exception {
        Class<?> clz = loadClass(fullClassName);
        if (null == clz) {
            synchronized (this) {
                clz = loadClass(fullClassName);
                if (null == clz) {
                    String code = null;
                    if (null == codeBuilder || null == (code = codeBuilder.build())) {
                        throw new RuntimeException("the codeBuilder or codeBuilder's result is null");
                    }
                    clz = doCompile(fullClassName, code, this.getClass().getClassLoader());
                }
            }
        }
        return clz;
    }

    /**
     * 加载class
     *
     * @param fullClassName
     * @return
     * @throws Exception
     */
    protected abstract Class<?> loadClass(String fullClassName) throws Exception;

    /**
     * 编译class
     *
     * @param fullClassName
     * @param code
     * @param classLoader
     * @return
     * @throws Exception
     */
    protected abstract Class<?> doCompile(String fullClassName, String code, ClassLoader classLoader) throws Exception;

}
