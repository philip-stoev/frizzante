package org.stoev.fuzzer;

import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

class VisitorValue implements Generatable {
	private final String methodName;

	VisitorValue(final String m) {
		methodName = m;
	}

	public void generate(final Context context, final Sentence<?> sentence) throws IOException {
		Object visitor = context.getVisitor();
		Class visitorClass = visitor.getClass();
		try {
			Method method = visitorClass.getDeclaredMethod(methodName, Context.class, Sentence.class);
			method.invoke(visitor, context, sentence);
		} catch (NoSuchMethodException e) {
			throw new ConfigurationException("Method " + methodName + " does not exist.");
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
