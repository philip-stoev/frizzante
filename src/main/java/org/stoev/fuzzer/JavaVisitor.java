package org.stoev.fuzzer;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

class JavaVisitor implements Generatable {
	private final String methodName;

	JavaVisitor(final String m) {
		methodName = m;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		Object visitor = context.getVisitor();
		Method methodObject = context.getCachedVisitor(methodName);

		if (methodObject == null) {
			try {
				Class visitorClass = visitor.getClass();
				methodObject = visitorClass.getDeclaredMethod(methodName, Context.class, Sentence.class);
				context.setCachedVisitor(methodName, methodObject);
			} catch (NoSuchMethodException e) {
				throw new ConfigurationException("Method " + methodName + " does not exist.");
			}
		}

		try {
			methodObject.invoke(visitor, context, sentence);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException("Attempting to invoke Visitor caused an IllegalAccessException");
		} catch (InvocationTargetException e) {
			throw new ConfigurationException("Visitor threw an exception: " + e.getMessage());
		}
	}

	public String toString() {
		return methodName;
	}

	public void compile(final Grammar grammar) {
		assert false;
	}
}
