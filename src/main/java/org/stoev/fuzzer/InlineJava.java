package org.stoev.fuzzer;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Iterator;

final class InlineJava<T> implements Generatable<T> {
	private final String className;
	private final String javaString;
	private Method javaMethod = null;

	InlineJava(final String cn, final String js) {
		javaString = js;
		className = cn;

		assert javaString != null;
		assert className != null;
		assert className.length() > 0;

		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(null, new String[] {
			"import org.stoev.fuzzer.ThreadContext",
			"import org.stoev.fuzzer.Sentence"
		});

		StringBuilder javaCode = new StringBuilder();
		javaCode.append("public static void generate(final ThreadContext<Object> threadContext, final Sentence<Object> sentence) {\n");
		javaCode.append(javaString);
		javaCode.append("}\n");

		javaCompiler.addJavaClass(className, javaCode.toString());
		javaCompiler.compileAll();

		Iterator<Class<?>> classIterator = javaCompiler.iterator();
		assert classIterator.hasNext();

		Class<?> javaClass = classIterator.next();
		assert !classIterator.hasNext();
		assert javaClass != null;

		Method[] javaMethods = javaClass.getDeclaredMethods();
		assert javaMethods.length == 1;

		javaMethod = javaMethods[0];
		assert javaMethod != null;
	}

	public void generate(final ThreadContext<T> threadContext, final Sentence<T> sentence) {
		try {

			if (threadContext.getGlobalContext().getGrammar().shouldCacheRule(className)) {
				Sentence<T> cachedSentence = sentence.newInstance();
				javaMethod.invoke(null, threadContext, cachedSentence);
	                        threadContext.setCachedValue(className, cachedSentence);
				sentence.addAll(cachedSentence);
			} else {
				javaMethod.invoke(null, threadContext, sentence);
			}
		} catch (IllegalAccessException illegalAccessException) {
			assert false : illegalAccessException.getMessage();
		} catch (InvocationTargetException invocationTargetException) {
			throw new IllegalArgumentException("Inline Java code threw an exception.", invocationTargetException.getCause());
		}
	}

	public void compile(final Grammar<T> grammar) {
		// do nothing at the compilation phase
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
