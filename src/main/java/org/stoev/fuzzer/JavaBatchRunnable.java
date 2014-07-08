package org.stoev.fuzzer;

import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

class JavaBatchRunnable extends FuzzRunnable {

	int getBatchSize() {
		return 100;
	}

	String[] getImports() {
		return new String[]{
			"import org.stoev.fuzzer.ThreadContext"
		};
	}

	private static final ConcurrentMap<GlobalContext<?>, ConcurrentMap<String, SoftReference<Method>>> METHOD_CACHES = new ConcurrentHashMap<GlobalContext<?>, ConcurrentMap<String, SoftReference<Method>>>();

	private final JavaBatchCompiler javaCompiler;

	public JavaBatchRunnable(final RunnableManager runnableManager, final ThreadContext<String> threadContext) {
		super(runnableManager, threadContext);

		ConcurrentMap<String, SoftReference<Method>> methodCache = new ConcurrentHashMap<String, SoftReference<Method>>();
		METHOD_CACHES.putIfAbsent(threadContext.getGlobalContext(), methodCache);
		methodCache = METHOD_CACHES.get(threadContext.getGlobalContext());
		assert methodCache != null;

		javaCompiler = new JavaBatchCompiler(methodCache, getImports());
	}

	public void run() {
		while (currentCount < threadContext.getGlobalContext().getCount()) {
			Map<String, Sentence<?>> classNameToSentence = new HashMap<String, Sentence<?>>();

			for (int n = 0; n < getBatchSize(); n++) {
				if (interrupted) {
					return;
				}

				if (currentCount >= threadContext.getGlobalContext().getCount()) {
					break;
				}

				currentCount++;

				Sentence<?> javaSentence = threadContext.generateSentence();
				String className = "Class" + javaSentence.getId();

				classNameToSentence.put(className, javaSentence);
				javaCompiler.addJavaClass(className, javaSentence.toString());
			}

			Iterable<Method> methodIterable;

			try {
				methodIterable = javaCompiler.compileAll();

				for (Method javaMethod: methodIterable) {
					if (interrupted) {
						return;
					}

					Sentence<?> sentence = null;

					try {
						sentence = classNameToSentence.get(javaMethod.getDeclaringClass().getName());
						this.invoke(javaMethod);
					} catch (InvocationTargetException invocationTargetException) {
						if (!interrupted) {
							executionException("Generated Java method returned an exception", invocationTargetException.getCause(), sentence);
						}
						return;
					} catch (IllegalAccessException illegalAccessException) {
						runtimeException(new IllegalArgumentException(illegalAccessException));
						return;
					}
				}
			} catch (RuntimeException runtimeException) {
				runtimeException(runtimeException);
				return;
			}


		}
	}

	public void invoke(final Method method) throws IllegalAccessException, InvocationTargetException {
		method.invoke(null, threadContext);
	}
}
