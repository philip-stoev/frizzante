package org.stoev.frizzante;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

import java.lang.ref.SoftReference;

public final class JavaBatchCompiler implements Iterable<Class<?>> {
	private static final String[] COMPILER_OPTIONS = new String[] {"-Xlint:all", "-Werror", "-source", "1.6", "-proc:none", "-implicit:none" };
	private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

	public static final ConcurrentMap<String, SoftReference<Class<?>>> GLOBAL_CLASS_CACHE = new ConcurrentHashMap<String, SoftReference<Class<?>>>();
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaBatchCompiler.class);

	private final String[] packageHeaders;
	private String packageName = null;

	private final ConcurrentMap<String, SoftReference<Class<?>>> classCache;
	private final Deque<JavaSource> javaSources = new ArrayDeque<JavaSource>();
	private final Deque<Class<?>> javaClasses = new ArrayDeque<Class<?>>();

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

	JavaBatchCompiler(final ConcurrentMap<String, SoftReference<Class<?>>> cache, final String[] headers) {
		this.classCache = cache;

		if (headers != null) {
			this.packageHeaders = headers;
		} else {
			this.packageHeaders = new String[]{};
		}

		for (String packageHeader: packageHeaders) {
			if (packageHeader.startsWith("package")) {
				this.packageName = packageHeader.substring("package".length() + 1);
			}
		}
	};

	void addJavaClass(final String className, final String javaString) {
		StringBuilder javaStringBuilder = new StringBuilder();

		for (String packageHeader: packageHeaders) {
			javaStringBuilder.append(packageHeader);
			javaStringBuilder.append(";\n");
		}

		javaStringBuilder.append("public class ");
		javaStringBuilder.append(className);
		javaStringBuilder.append("{");
		javaStringBuilder.append(javaString);
                javaStringBuilder.append("}");

		final String fullClassName;

		if (packageName != null) {
			fullClassName = packageName + '.' + className;
		} else {
			fullClassName = className;
		}

		javaSources.addLast(new JavaSource(fullClassName, javaStringBuilder.toString()));
	}

	public Iterable<Class<?>> compileAll() {
		final List<JavaFileObject> javaFileObjects = new ArrayList<JavaFileObject>();
		final Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();

		// For each Class to be compiled, check if it exists in the cache, or if it was seen already in the current batch

		for (JavaSource javaSource: javaSources) {
			String className = javaSource.getClassName();

			if (!classMap.containsKey(className)) {
				Class<?> javaClass = getCachedClass(className);

				if (javaClass == null) {
					javaFileObjects.add(new JavaSourceInMemory(className, javaSource.getJavaString()));
					// Put null in the classMap now to signal that we need to fetch the class from the compiler later
					classMap.put(className, null);
				} else {
					classMap.put(className, javaClass);
				}
			}
		}

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		final JavaFileManager fileManager = new FileManagerInMemory(COMPILER.getStandardFileManager(diagnostics, null, null));

		if (!javaFileObjects.isEmpty()) {
			boolean compilationSuccess = COMPILER.getTask(null, fileManager, diagnostics, Arrays.asList(COMPILER_OPTIONS), null, javaFileObjects).call();

			if (!compilationSuccess) {
				StringBuilder sb = new StringBuilder();

				sb.append("Java compilation failed:\n");
				for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
					sb.append(diagnostic.toString());
					sb.append(System.getProperty("line.separator"));
				}

				LOGGER.error(sb.toString());
				throw new IllegalArgumentException(sb.toString());
			}

			for (Map.Entry<String, Class<?>> entry : classMap.entrySet()) {
				if (entry.getValue() != null) {
					continue;
				}

				try {
					String className = entry.getKey();
					Class<?> javaClass = fileManager.getClassLoader(null).loadClass(className);
					assert javaClass != null;

					classMap.put(className, javaClass);
					setCachedClass(className, javaClass);
				} catch (ClassNotFoundException e) {
					assert false : e.getMessage();
				}
			}
		}

		while (!javaSources.isEmpty()) {
			JavaSource javaSource = javaSources.removeFirst();
			Class<?> javaClass = classMap.get(javaSource.getClassName());
			assert javaClass != null : javaSource.getClassName();
			javaClasses.addLast(javaClass);
		}

		return javaClasses;
	}

	public Iterator<Class<?>> iterator() {
		return javaClasses.iterator();
	}

	void setCachedClass(final String className, final Class<?> javaClass) {
		if (classCache != null) {
			SoftReference<Class<?>> softReference = new SoftReference<Class<?>>(javaClass);
			classCache.putIfAbsent(className, softReference);
		}
	}

	Class<?> getCachedClass(final String className) {
		if (classCache == null) {
			return null;
		}

		SoftReference<Class<?>> softReference = classCache.get(className);

		if (softReference != null) {
			final Class<?> javaClass = softReference.get();
			if (javaClass != null) {
				return javaClass;
			} else {
				classCache.remove(className);
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

	JavaClassInMemory(final String className, final Kind kind) {
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

	FileManagerInMemory(final StandardJavaFileManager standardManager) {
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
