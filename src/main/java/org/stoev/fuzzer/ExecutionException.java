package org.stoev.fuzzer;

@SuppressWarnings("serial")
public class ExecutionException extends Exception {
	private final transient ThreadContext<?> threadContext;
	private final transient Sentence<?> sentence;

	ExecutionException(final String message, final Throwable cause, final ThreadContext<?> threadContext, final Sentence<?> sentence) {
		super(message, cause);

		assert threadContext != null;
		assert sentence != null;

		this.threadContext = threadContext;
		this.sentence = sentence;
	}

	public final ThreadContext<?> getThreadContext() {
		return threadContext;
	}

	public final Sentence<?> getSentence() {
		return sentence;
	}

	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Grammar:\n");
		sb.append(threadContext.getGlobalContext().getGrammar().toString());

		sb.append("Global context:\n");
		sb.append(threadContext.getGlobalContext().toString());

		sb.append("Local context:\n");
		sb.append(threadContext.toString());

		return sb.toString();
	}
}
