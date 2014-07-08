package org.stoev.fuzzer;

abstract class FuzzRunnable implements Runnable {

	protected final RunnableManager runnableManager;
	protected final ThreadContext<?> threadContext;
	protected volatile boolean interrupted = false;
	protected long currentCount = 0;

	protected ExecutionException executionException = null;
	protected RuntimeException runtimeException = null;

	FuzzRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		assert runnableManager != null;
		assert threadContext != null;

		this.runnableManager = runnableManager;
		this.threadContext = threadContext;
	}

	public abstract void run();

	public void interrupt() {
		interrupted = true;
	}

	public ExecutionException getExecutionException() {
		return executionException;
	}

	public RuntimeException getRuntimeException() {
		return runtimeException;
	}

	protected void executionException(final String message, final Throwable cause, final Sentence<?> sentence) {
		assert sentence != null;

		executionException = new ExecutionException(message, cause, threadContext, sentence);
		runnableManager.killAll();
	}

	protected void runtimeException(final RuntimeException cause) {
		runtimeException = cause;
		runnableManager.killAll();
	}
}
