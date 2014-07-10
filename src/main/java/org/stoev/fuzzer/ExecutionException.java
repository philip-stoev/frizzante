package org.stoev.fuzzer;

@SuppressWarnings("serial")
public class ExecutionException extends Exception {
	private final transient ThreadContext<?> threadContext;
	private final transient Sentence<?> sentence;
	private final transient FuzzRunnable runnable;

	ExecutionException(final String message, final Throwable cause, final ThreadContext<?> threadContext, final FuzzRunnable runnable, final Sentence<?> sentence) {
		super(message, cause);

		assert threadContext != null;
		assert runnable != null;
		assert sentence != null;

		this.threadContext = threadContext;
		this.runnable = runnable;
		this.sentence = sentence;
	}

	public final ThreadContext<?> getThreadContext() {
		return threadContext;
	}

	public final Sentence<?> getSentence() {
		return sentence;
	}

	public final String getMessage() {
		StringBuilder sb = new StringBuilder();

		sb.append("Cause: ");
		sb.append(getCause().toString());
		sb.append("\n");

		sb.append("Global context:\n");
		sb.append(threadContext.getGlobalContext().toString());

		sb.append("Thread context:\n");
		sb.append(threadContext.toString());

		sb.append("Execution counter: ");
		sb.append(runnable.getExecutionCounter());
		sb.append("\n");

		sb.append("Sentence:\n");
		sb.append("Sentence id:");
		sb.append(sentence.getId());
		sb.append("\n");
		sb.append(sentence.toString());

		return sb.toString();
	}
}
