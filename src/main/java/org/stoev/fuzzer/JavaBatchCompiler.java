package org.stoev.fuzzer;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;
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

	private static final ConcurrentHashMap<String, SoftReference<Method>> METHOD_CACHE = new ConcurrentHashMap<String, SoftReference<Method>>();

	private final String packageName;
	private final String[] imports;
	private final String methodName;
	private final Class[] methodParameters;

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

	JavaBatchCompiler(final String pn, final String mn, final Class[] mp, final String[] imp) {
		packageName = pn;
		methodName = mn;
		methodParameters = mp;
		imports = imp;
	};

	void addJava(final String className, final String javaString) {
		StringBuilder javaStringBuilder = new StringBuilder();

		javaStringBuilder.append("package ");
		javaStringBuilder.append(packageName);
		javaStringBuilder.append(";\n\n");

		for (String importDeclaration: imports) {
			javaStringBuilder.append("import ");
			javaStringBuilder.append(importDeclaration);
			javaStringBuilder.append(";\n");
		}

		javaStringBuilder.append("public class ");
		javaStringBuilder.append(className);
		javaStringBuilder.append("{");
		javaStringBuilder.append(javaString);
                javaStringBuilder.append("}");

		javaSources.addLast(new JavaSource(className, javaStringBuilder.toString()));
	}

	void compileAll() {
		final List<JavaFileObject> javaFileObjects = new ArrayList<JavaFileObject>();
		final JavaFileManager fileManager = new FileManagerInMemory(COMPILER.getStandardFileManager(null, null, null));

		final Map<String, Method> methodMap = new HashMap<String, Method>();

		// For each Class to be compiled, check if it exists in the global cache, or if it was seen already in the current batch

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
				throw new ConfigurationException("Java code compilation failed.");
			}

			for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
				if (entry.getValue() != null) {
					continue;
				}

				try {
					String className = entry.getKey();
					Class<?> javaClass = fileManager.getClassLoader(null).loadClass(packageName + "." + className);
					assert javaClass != null;

					Method javaMethod = javaClass.getDeclaredMethod(methodName, methodParameters);
					assert javaMethod != null;

					methodMap.put(className, javaMethod);
					setCachedMethod(className, javaMethod);
				} catch (ClassNotFoundException e) {
					assert false : e.getMessage();
				} catch (NoSuchMethodException e) {
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
	}

	public Iterator<Method> iterator() {
		return javaMethods.iterator();
	}

	void setCachedMethod(final String className, final Method method) {
		SoftReference<Method> softReference = new SoftReference<Method>(method);
		METHOD_CACHE.putIfAbsent(className, softReference);
	}

	Method getCachedMethod(final String className) {
		SoftReference<Method> softReference = METHOD_CACHE.get(className);

		if (softReference != null) {
			final Method method = softReference.get();
			if (method != null) {
				return method;
			} else {
				METHOD_CACHE.remove(className);
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
