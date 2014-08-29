package org.stoev.frizzante;

public interface FuzzRunnableFactory {
	FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext);
}
