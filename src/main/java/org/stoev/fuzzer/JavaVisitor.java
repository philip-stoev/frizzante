package org.stoev.fuzzer;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

final class JavaVisitor implements Generatable {
	private final String methodName;
	private final Method methodObject;
	private final Object visitor;
	private final Generatable argument;

	JavaVisitor(final Object v, final String mn, final Generatable arg) {
		visitor = v;
		assert visitor != null;

		methodName = mn;
		assert methodName != null;
		assert methodName.length() > 0;

		argument = arg;

		try {
			Class visitorClass = visitor.getClass();
			methodObject = visitorClass.getDeclaredMethod(methodName, Context.class, Sentence.class, Sentence.class);
		} catch (NoSuchMethodException e) {
			throw new ConfigurationException("Method " + methodName + " in visitor class " + visitor.getClass() + " does not have the correct signature.");
		}

		if (!methodObject.getReturnType().equals(Void.TYPE)) {
			throw new ConfigurationException("Visitor must be declared as void.");
		}

		assert methodObject != null;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		Sentence<?> argumentSentence = null;

		if (argument != null) {
			// If this visitor has arguments, we generate them into a temporary Sentence
			// so that we can pass them to the visitor

			argumentSentence = sentence.newInstance();
			argumentSentence.getStack().push(argument);

			while (!argumentSentence.getStack().isEmpty()) {
				argumentSentence.getStack().pop().generate(context, argumentSentence);
			}
		}

		try {
			methodObject.invoke(visitor, context, sentence, argumentSentence);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException("Attempting to invoke visitor " + methodName + " in class " + visitor.getClass() + " caused an IllegalAccessException");
		} catch (InvocationTargetException e) {
			throw new ConfigurationException("Visitor " + methodName + " in class " +  visitor.getClass() + " threw an exception: " + e.getMessage());
		}
	}

	public String toString() {
		return methodName;
	}

	public String getName() {
		return methodName;
	}

	public void compile(final Grammar grammar) {
		if (argument != null) {
			argument.compile(grammar);
		}
	}
}
