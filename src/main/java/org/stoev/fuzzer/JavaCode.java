package org.stoev.fuzzer;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

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

public class JavaCode implements Generatable {
	private final String className;
	private final String javaString;
	private Generatable javaObject = null;

        JavaCode(final String cn, final String js) {
		javaString = js;
		className = cn;
		String fullClassName = "org.stoev.fuzzer.embedded." + cn;

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		StringBuilder javaCode = new StringBuilder();

		javaCode.append("package org.stoev.fuzzer.embedded;\n");
		javaCode.append("import org.stoev.fuzzer.Generatable;\n");
		javaCode.append("import org.stoev.fuzzer.Grammar;\n");
		javaCode.append("import org.stoev.fuzzer.Context;\n");
		javaCode.append("import org.stoev.fuzzer.Sentence;\n");
		javaCode.append("import java.io.IOException;\n");
		javaCode.append("public class " + className + " implements Generatable {\n");
		javaCode.append("	protected Object storage;\n");
		javaCode.append("	public void compile(final Grammar grammar) { } ;");
		javaCode.append("	public void generate(final Context context, final Sentence<?> sentence) {\n");
		javaCode.append(javaString);
		javaCode.append("	}\n");
		javaCode.append("}\n");

		JavaFileManager fileManager = new FileManagerInMemory(compiler.getStandardFileManager(null, null, null));
		List<JavaFileObject> javaFiles = new ArrayList<JavaFileObject>();
		javaFiles.add(new JavaSourceInMemory(fullClassName, javaCode.toString()));

		String[] compilerOptions = new String[] {"-Xlint:all", "-Werror", "-source", "1.6" };

		boolean compilationSuccess = compiler.getTask(null, fileManager, null, Arrays.asList(compilerOptions), null, javaFiles).call();

		if (!compilationSuccess) {
			throw new ConfigurationException();
		}

		try {
			javaObject = (Generatable) fileManager.getClassLoader(null).loadClass(fullClassName).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
        }

	public final void generate(final Context context, final Sentence<?> sentence) {
		javaObject.generate(context, sentence);
	}

	public void compile(final Grammar grammar) {

	}

	public final String toString() {
		assert false;
		return "foo";
	}
}

/*
*
* The code below is adapted from http://www.javablogging.com/dynamic-in-memory-compilation/
*
*/

class JavaSourceInMemory extends SimpleJavaFileObject {
	private final String javaString;

	JavaSourceInMemory(final String cn, final String js) {
		super(URI.create("string:///" + cn.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		javaString = js;
	}

	@Override
	public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
		return javaString;
	}
}


class JavaClassInMemory extends SimpleJavaFileObject {
	private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

	public JavaClassInMemory(final String name, final Kind kind) {
		super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
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
	private JavaClassInMemory javaClassInMemory;

	public FileManagerInMemory(final StandardJavaFileManager standardManager) {
		super(standardManager);
	}

	public ClassLoader getClassLoader(final Location location) {
		return new SecureClassLoader() {
			@Override
        		protected Class<?> findClass(final String name) throws ClassNotFoundException {
				byte[] b = javaClassInMemory.getBytes();
		                return super.defineClass(name, b, 0, b.length);
			}
		};
	}

	@Override
	public JavaFileObject getJavaFileForOutput(final Location location, final String className, final Kind kind, final FileObject sibling) throws IOException {
		javaClassInMemory = new JavaClassInMemory(className, kind);
	        return javaClassInMemory;
	}
}
