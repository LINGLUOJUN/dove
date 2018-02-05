package com.six.dove.remote.compiler.support;

import com.six.dove.util.ClassUtils;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * <p>@description:</p>
 *
 * @author yangshuang
 * @version v1.0
 * @date 2018/2/5 16:43
 */
public class StringJavaObject extends SimpleJavaFileObject {

    /**
     * 源代码
     * **
     */
    private CharSequence content;

    private ByteArrayOutputStream bytecode;

    // 遵循Java规范的类名及文件
    public StringJavaObject(String javaFileName, CharSequence content) {
        super(ClassUtils.toURI(javaFileName + ClassUtils.JAVA_EXTENSION), Kind.SOURCE);
        this.content = content;
    }

    public StringJavaObject(URI uri, Kind kind) {
        super(uri, kind);
        content = null;
    }

    public StringJavaObject(final String name, final Kind kind) {
        super(ClassUtils.toURI(name), kind);
        content = null;
    }

    // 文本文件代码
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return content;
    }

    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(getByteCode());
    }

    @Override
    public OutputStream openOutputStream() {
        return bytecode = new ByteArrayOutputStream();
    }

    public byte[] getByteCode() {
        return bytecode.toByteArray();
    }

}
