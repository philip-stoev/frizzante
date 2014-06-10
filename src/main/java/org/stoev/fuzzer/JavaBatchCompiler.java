package org.stoev.fuzzer;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;

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

final class JavaBatchCompiler implements Iterable<Class<?>> {
	static final String[] COMPILER_OPTIONS = new String[] {"-Xlint:all", "-Werror", "-source", "1.6", "-g:none", "-proc:none", "-implicit:none" };
	static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

	private final String packageName;
	private final String[] imports;
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

	JavaBatchCompiler(final String pn, final String[] imp) {
		packageName = pn;
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

		javaStringBuilder.append("\npublic class ");
		javaStringBuilder.append(className);
		javaStringBuilder.append(" {\n");
		javaStringBuilder.append(javaString);
		javaStringBuilder.append("}\n");

		javaSources.addLast(new JavaSource(className, javaStringBuilder.toString()));
	}

	void compileAll() {
		final List<JavaFileObject> javaFileObjects = new ArrayList<JavaFileObject>();
		final JavaFileManager fileManager = new FileManagerInMemory(COMPILER.getStandardFileManager(null, null, null));
		final List<String> classNames = new ArrayList<String>();

		while (!javaSources.isEmpty()) {
			JavaSource javaSource = javaSources.removeFirst();
			javaFileObjects.add(new JavaSourceInMemory(javaSource.getClassName(), javaSource.getJavaString()));
			classNames.add(javaSource.getClassName());
		}

		boolean compilationSuccess = COMPILER.getTask(null, fileManager, null, Arrays.asList(COMPILER_OPTIONS), null, javaFileObjects).call();

		if (!compilationSuccess) {
			throw new ConfigurationException("Java code compilation failed.");
		}

		for (String className: classNames) {
			try {
				Class<?> javaClass = fileManager.getClassLoader(null).loadClass(packageName + "." + className);
				assert javaClass != null;

				javaClasses.addLast(javaClass);
			} catch (ClassNotFoundException e) {
				assert false : e.getMessage();
			}
		}
	}

	public Iterator<Class<?>> iterator() {
		return javaClasses.iterator();
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
