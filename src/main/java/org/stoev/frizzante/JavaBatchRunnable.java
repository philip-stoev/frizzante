package org.stoev.frizzante;

import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class JavaBatchRunnable extends FuzzRunnable {
	private static final ConcurrentMap<GlobalContext<?>, ConcurrentMap<String, SoftReference<Class<?>>>> CLASS_CACHES = new ConcurrentHashMap<GlobalContext<?>, ConcurrentMap<String, SoftReference<Class<?>>>>();
	public static final String CLASS_PREFIX = "GeneratedClass";

	private final JavaBatchCompiler javaCompiler;

	public JavaBatchRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		super(runnableManager, threadContext);

		ConcurrentMap<String, SoftReference<Class<?>>> classCache = new ConcurrentHashMap<String, SoftReference<Class<?>>>();
		CLASS_CACHES.putIfAbsent(threadContext.getGlobalContext(), classCache);
		classCache = CLASS_CACHES.get(threadContext.getGlobalContext());
		assert classCache != null;

		javaCompiler = new JavaBatchCompiler(classCache, getHeaders());
	}

	@SuppressWarnings("checkstyle:designforextension")
	protected int getBatchSize() {
		return 100;
	}

	@SuppressWarnings("checkstyle:designforextension")
	protected String[] getHeaders() {
		return new String[]{
			"import org.stoev.frizzante.ThreadContext"
		};
	}

	@Override
	public final void run() {
		while (executionCounter < threadContext.getGlobalContext().getCount()) {
			Map<String, Sentence<?>> classNameToSentence = new HashMap<String, Sentence<?>>();

			long currentBatchSize = Math.min(getBatchSize(), threadContext.getGlobalContext().getCount() - executionCounter);

			for (int n = 0; n < currentBatchSize; n++) {
				if (interrupted) {
					return;
				}

				Sentence<?> javaSentence = threadContext.generateSentence();
				String className = CLASS_PREFIX + javaSentence.getId();

				classNameToSentence.put(className, javaSentence);
				javaCompiler.addJavaClass(className, javaSentence.toString());
			}

			try {
				for (Class<?> javaClass: javaCompiler.compileAll()) {
					if (interrupted) {
						return;
					}

					Method[] javaMethods = javaClass.getDeclaredMethods();
					Method javaMethod = javaMethods[0];

					Sentence<?> sentence = null;

					try {
						sentence = classNameToSentence.get(javaClass.getName());
						executionCounter++;
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

	@SuppressWarnings("checkstyle:designforextension")
	public void invoke(final Method method) throws IllegalAccessException, InvocationTargetException {
		method.invoke(null);
	}
}
