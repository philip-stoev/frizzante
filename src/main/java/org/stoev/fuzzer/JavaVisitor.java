package org.stoev.fuzzer;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

final class JavaVisitor implements Generatable {
	private final String methodName;
	private final Method methodObject;
	private final Object visitor;

	JavaVisitor(final Object v, final String mn) {
		visitor = v;
		assert visitor != null;

		methodName = mn;
		assert methodName != null;
		assert methodName.length() > 0;

		try {
			Class visitorClass = visitor.getClass();
			methodObject = visitorClass.getDeclaredMethod(methodName, Context.class, Sentence.class);
		} catch (NoSuchMethodException e) {
			throw new ConfigurationException("Method " + methodName + " in visitor class " + visitor.getClass() + " does not have the correct signature.");
		}

		if (!methodObject.getReturnType().equals(Void.TYPE)) {
			throw new ConfigurationException("Visitor must be declared as void.");
		}

		assert methodObject != null;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
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

	public String getName() {
		return methodName;
	}

	public void compile(final Grammar grammar) {

	}
}
