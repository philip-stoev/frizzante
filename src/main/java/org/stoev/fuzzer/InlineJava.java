package org.stoev.fuzzer;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Iterator;

final class InlineJava<T> implements Generatable<T> {
	private final String className;
	private final String javaString;
	private Method javaMethod = null;

	private static final String METHOD_NAME = "generate";

	InlineJava(final String cn, final String js) {
		javaString = js;
		className = cn;

		assert javaString != null;
		assert className != null;
		assert className.length() > 0;

		JavaBatchCompiler javaCompiler = new JavaBatchCompiler("org.stoev.fuzzer.embedded", METHOD_NAME,
		new Class<?>[] {
			Context.class,
			Sentence.class
		}, new String[] {
			"org.stoev.fuzzer.Context"
			, "org.stoev.fuzzer.Sentence"
		});

		StringBuilder javaCode = new StringBuilder();
		javaCode.append("	public static void ");
		javaCode.append(METHOD_NAME);
		javaCode.append("(final Context<Object> context, final Sentence<Object> sentence) {\n");
		javaCode.append(javaString);
		javaCode.append("	}\n");

		javaCompiler.addJava(className, javaCode.toString());
		javaCompiler.compileAll();

		Iterator<Method> classIterator = javaCompiler.iterator();
		assert classIterator.hasNext();

		javaMethod = classIterator.next();
		assert javaMethod != null;
	}

	public void generate(final Context<T> context, final Sentence<T> sentence) {
		try {

			if (context.shouldCacheRule(className)) {
				Sentence<T> cachedSentence = sentence.newInstance();
				javaMethod.invoke(null, context, cachedSentence);
	                        context.setCachedValue(className, cachedSentence);
				sentence.addAll(cachedSentence);
			} else {
				javaMethod.invoke(null, context, sentence);
			}
		} catch (IllegalAccessException e) {
			assert false : e.getMessage();
		} catch (InvocationTargetException e) {
			throw new ConfigurationException("Inline Java code threw an exception: " + e.getMessage());
		}
	}

	public void compile(final Grammar<T> grammar) {

	}

	public boolean isConstant() {
		return false;
	}

	public String toString() {
		return className + ":" + javaString + "\n";
	}

	public String getName() {
		assert false;
		return className;
	}
}
