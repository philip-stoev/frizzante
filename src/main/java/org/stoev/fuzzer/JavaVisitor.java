package org.stoev.fuzzer;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

class JavaVisitor implements Generatable {
	private final String methodName;

	JavaVisitor(final String m) {
		methodName = m;

		assert methodName != null;
		assert methodName.length() > 0;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		Object visitor = context.getVisitor();

		if (visitor == null) {
			throw new ConfigurationException("Grammar uses Visitors, but no Visitor specified.");
		}

		Method methodObject = context.getCachedVisitor(methodName);

		if (methodObject == null) {
			try {
				Class visitorClass = visitor.getClass();
				methodObject = visitorClass.getDeclaredMethod(methodName, Context.class, Sentence.class);
				context.setCachedVisitor(methodName, methodObject);
			} catch (NoSuchMethodException e) {
				throw new ConfigurationException("Method " + methodName + " does not exist.");
			}

			if (!methodObject.getReturnType().equals(Void.TYPE)) {
				throw new ConfigurationException("Visitor must be declared as void.");
			}
		}

		assert methodObject != null;

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
		assert false;
	}
}
