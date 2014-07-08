package org.stoev.fuzzer;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import java.net.URI;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.security.SecureClassLoader;

import javax.tools.ToolProvider;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileObject.Kind;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.FileObject;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;

final class JavaBatchCompiler implements Iterable<Method> {
	private static final String[] COMPILER_OPTIONS = new String[] {"-Xlint:all", "-Werror", "-source", "1.6", "-g:none", "-proc:none", "-implicit:none" };
	private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

	public static final ConcurrentMap<String, SoftReference<Method>> GLOBAL_METHOD_CACHE = new ConcurrentHashMap<String, SoftReference<Method>>();

	private final String[] packageHeaders;

	private final ConcurrentMap<String, SoftReference<Method>> methodCache;
	private final Deque<JavaSource> javaSources = new ArrayDeque<JavaSource>();
	private final Deque<Method> javaMethods = new ArrayDeque<Method>();

	static class JavaSource {
		private final String className;
		private final String javaString;

		JavaSource(final String cn, final String js) {
			className = cn;
			javaString = js;
		}

		String getClassName() {
			return className;
		}

		String getJavaString() {
			return javaString;
		}
	}

	JavaBatchCompiler(final ConcurrentMap<String, SoftReference<Method>> cache, final String[] headers) {
		this.methodCache = cache;
		this.packageHeaders = headers;
	};

	void addJavaClass(final String className, final String javaString) {
		StringBuilder javaStringBuilder = new StringBuilder();

		if (packageHeaders != null) {
			for (String packageHeader: packageHeaders) {
				javaStringBuilder.append(packageHeader);
				javaStringBuilder.append(";\n");
			}
		}

		javaStringBuilder.append("public class ");
		javaStringBuilder.append(className);
		javaStringBuilder.append("{");
		javaStringBuilder.append(javaString);
                javaStringBuilder.append("}");

		javaSources.addLast(new JavaSource(className, javaStringBuilder.toString()));
	}

	public Iterable<Method> compileAll() {
		final List<JavaFileObject> javaFileObjects = new ArrayList<JavaFileObject>();
		final JavaFileManager fileManager = new FileManagerInMemory(COMPILER.getStandardFileManager(null, null, null));

		final Map<String, Method> methodMap = new HashMap<String, Method>();

		// For each Class to be compiled, check if it exists in the cache, or if it was seen already in the current batch

		for (JavaSource javaSource: javaSources) {
			String className = javaSource.getClassName();

			if (!methodMap.containsKey(className)) {
				Method javaMethod = getCachedMethod(className);

				if (javaMethod == null) {
					javaFileObjects.add(new JavaSourceInMemory(className, javaSource.getJavaString()));
					// Put null in the methodMap now to signal that we need to fetch the method from the compiler later
					methodMap.put(className, null);
				} else {
					methodMap.put(className, javaMethod);
				}
			}
		}

		if (!javaFileObjects.isEmpty()) {
			boolean compilationSuccess = COMPILER.getTask(null, fileManager, null, Arrays.asList(COMPILER_OPTIONS), null, javaFileObjects).call();

			if (!compilationSuccess) {
				throw new IllegalArgumentException("Java code compilation failed.");
			}

			for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
				if (entry.getValue() != null) {
					continue;
				}

				try {
					String className = entry.getKey();
					Class<?> javaClass = fileManager.getClassLoader(null).loadClass(className);
					assert javaClass != null;

					Method[] javaMethods = javaClass.getDeclaredMethods();
					Method javaMethod = javaMethods[0];
					assert javaMethod != null;

					methodMap.put(className, javaMethod);
					setCachedMethod(className, javaMethod);
				} catch (ClassNotFoundException e) {
					assert false : e.getMessage();
				}
			}
		}

		while (!javaSources.isEmpty()) {
			JavaSource javaSource = javaSources.removeFirst();
			Method javaMethod = methodMap.get(javaSource.getClassName());
			assert javaMethod != null : javaSource.getClassName();
			javaMethods.addLast(javaMethod);
		}

		return javaMethods;
	}

	public Iterator<Method> iterator() {
		return javaMethods.iterator();
	}

	void setCachedMethod(final String className, final Method method) {
		if (methodCache != null) {
			SoftReference<Method> softReference = new SoftReference<Method>(method);
			methodCache.putIfAbsent(className, softReference);
		}
	}

	Method getCachedMethod(final String className) {
		if (methodCache == null) {
			return null;
		}

		SoftReference<Method> softReference = methodCache.get(className);

		if (softReference != null) {
			final Method method = softReference.get();
			if (method != null) {
				return method;
			} else {
				methodCache.remove(className);
			}
		}

		return null;
	}
}

/*
*
* The code below is adapted from http://www.javablogging.com/dynamic-in-memory-compilation/
*
*/

class JavaSourceInMemory extends SimpleJavaFileObject {
	private final String javaString;

	JavaSourceInMemory(final String className, final String js) {
		super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		javaString = js;
	}

	@Override
	public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
		return javaString;
	}
}

class JavaClassInMemory extends SimpleJavaFileObject {
	private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

	public JavaClassInMemory(final String className, final Kind kind) {
		super(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind);
	}

	public byte[] getBytes() {
		return bos.toByteArray();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return bos;
	}
}

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED")
class FileManagerInMemory extends ForwardingJavaFileManager<JavaFileManager> {
	private Map<String, JavaClassInMemory> javaClassesInMemory = new HashMap<String, JavaClassInMemory>();

	public FileManagerInMemory(final StandardJavaFileManager standardManager) {
		super(standardManager);
	}

	public ClassLoader getClassLoader(final Location location) {
		return new SecureClassLoader() {
			@Override
			protected Class<?> findClass(final String className) throws ClassNotFoundException {
				JavaClassInMemory javaClassInMemory = javaClassesInMemory.get(className);

				byte[] b = javaClassInMemory.getBytes();
				return super.defineClass(className, b, 0, b.length);
			}
		};
	}

	@Override
	public JavaFileObject getJavaFileForOutput(final Location location, final String className, final Kind kind, final FileObject sibling) throws IOException {
		JavaClassInMemory javaClassInMemory = new JavaClassInMemory(className, kind);
		javaClassesInMemory.put(className, javaClassInMemory);

		return javaClassInMemory;
	}
}
