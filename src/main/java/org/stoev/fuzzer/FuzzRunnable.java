package org.stoev.fuzzer;

public abstract class FuzzRunnable implements Runnable {

	protected final RunnableManager runnableManager;
	protected final ThreadContext<?> threadContext;
	protected volatile boolean interrupted = false;
	protected long executionCounter = 0;

	protected ExecutionException executionException = null;
	protected RuntimeException runtimeException = null;

	FuzzRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		assert runnableManager != null;
		assert threadContext != null;

		this.runnableManager = runnableManager;
		this.threadContext = threadContext;
	}

	public abstract void run();

	public final void interrupt() {
		interrupted = true;
	}

	public final long getExecutionCounter() {
		return executionCounter;
	}

	public final ExecutionException getExecutionException() {
		return executionException;
	}

	public final RuntimeException getRuntimeException() {
		return runtimeException;
	}

	protected final void executionException(final String message, final Throwable cause, final Sentence<?> sentence) {
		assert sentence != null;

		executionException = new ExecutionException(message, cause, threadContext, this, sentence);
		runnableManager.killAll();
	}

	protected final void runtimeException(final RuntimeException cause) {
		runtimeException = cause;
		runnableManager.killAll();
	}
}
