package com.six.dove.remote.compiler;

import com.six.dove.remote.compiler.support.DoveJavaFileManager;
import com.six.dove.remote.compiler.support.ProxyClassLoader;
import com.six.dove.remote.compiler.support.StringJavaObject;
import com.six.dove.util.ClassUtils;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class DoveJdkCompiler extends AbstractCompiler {

	private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

	private final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

	private final DoveJavaFileManager javaFileManager;


	private ProxyClassLoader proxyClassLoader;

	public DoveJdkCompiler() {
		StandardJavaFileManager manager = compiler.getStandardFileManager(diagnosticCollector, null, null);
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		proxyClassLoader = AccessController.doPrivileged((PrivilegedAction<ProxyClassLoader>) () -> new ProxyClassLoader(loader));
		javaFileManager = new DoveJavaFileManager(manager, proxyClassLoader);
	}

	@Override
	protected Class<?> loadClass(String fullClassName) throws Exception {
		return proxyClassLoader.loadClass(fullClassName);
	}

	@Override
	protected Class<?> doCompile(String fullClassName, String code, ClassLoader classLoader) throws Exception {

		String packageName =fullClassName.substring(0,fullClassName.lastIndexOf("."));
		String className = fullClassName.substring(fullClassName.lastIndexOf(".")+1);

		JavaFileObject javaFileObject = new StringJavaObject(className, code);

		javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName,
				className + ClassUtils.JAVA_EXTENSION, javaFileObject);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		CompilationTask compilationTask = compiler.getTask(null, javaFileManager, diagnostics, null, null,
				Arrays.asList(javaFileObject));
		Boolean result = compilationTask.call();
		if (result == null || !result.booleanValue()) {
			throw new IllegalStateException("Compilation failed. class: " + fullClassName + ", diagnostics: ");
		}
		return proxyClassLoader.loadClass(fullClassName);
	}


}
