package org.stoev.fuzzer;

public abstract class FuzzRunnableFactory {
	public abstract FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext);
}
