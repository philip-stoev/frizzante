package org.stoev.fuzzer;

public class JavaBatchRunnableFactory extends FuzzRunnableFactory {
	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		return new JavaBatchRunnable(runnableManager, threadContext);
	}
}