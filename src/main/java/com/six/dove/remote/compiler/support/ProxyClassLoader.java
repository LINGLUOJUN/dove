package com.six.dove.remote.compiler.support;

import javax.tools.JavaFileObject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>@description:类加载器</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/5 16:41
 */
public class ProxyClassLoader extends ClassLoader {

    private final Map<String, JavaFileObject> classes = new HashMap<>();

    public ProxyClassLoader(ClassLoader parentClassLoader) {
        super(parentClassLoader);
    }

    @Override
    protected Class<?> findClass(final String className) {
        JavaFileObject file = classes.get(className);
        if (null != file) {
            byte[] bytes = ((StringJavaObject) file).getByteCode();
            return defineClass(className, bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    Collection<JavaFileObject> files() {
        return Collections.unmodifiableCollection(classes.values());
    }

    void add(final String qualifiedClassName, final JavaFileObject javaFile) {
        classes.put(qualifiedClassName, javaFile);
    }
}
