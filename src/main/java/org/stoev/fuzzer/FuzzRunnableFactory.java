package org.stoev.fuzzer;

public interface FuzzRunnableFactory {
	FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext);
}
