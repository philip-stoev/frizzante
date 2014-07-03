package org.stoev.fuzzer;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

abstract class JavaBatchRunnable extends FuzzRunnable {

	int getBatchSize() {
		return 100;
	}

	String[] getImports() {
		return new String[]{};
	}

	final JavaBatchCompiler javaCompiler = new JavaBatchCompiler(true, "org.stoev.fuzzer", getImports());
	long currentCount = 0;

	JavaBatchRunnable(final ThreadContext<String> threadContext) {
		super(threadContext);
	}

	public void run() {
		while (currentCount < threadContext.getGlobalContext().getCount()) {
			for (int n = 0; n < getBatchSize(); n++) {
				if (interrupted) {
					return;
				}

				currentCount++;

				Sentence<?> javaSentence = threadContext.generateSentence();
				String className = "Class" + javaSentence.getId();

				javaCompiler.addJavaClass(className, javaSentence.toString());
			}

			for (Method javaMethod: javaCompiler.compileAll()) {
				if (interrupted) {
					return;
				}

				try {
					this.invoke(javaMethod);
				} catch (IllegalAccessException e) {
					assert false;
				} catch (InvocationTargetException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
	}

	public void invoke(Method method) throws IllegalAccessException, InvocationTargetException {
		method.invoke(null, threadContext);
	}
}
