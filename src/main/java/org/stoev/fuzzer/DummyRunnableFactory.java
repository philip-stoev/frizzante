package org.stoev.fuzzer;

public class DummyRunnableFactory extends FuzzRunnableFactory {
	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		return new DummyRunnable(runnableManager, threadContext);
	}
}

class DummyRunnable extends FuzzRunnable {
	DummyRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
                super(runnableManager, threadContext);
	}

	@Override
	public final void run() {
		while (executionCounter < threadContext.getGlobalContext().getCount()) {
			Sentence<?> javaSentence = threadContext.generateSentence();
			System.out.println(javaSentence);
			executionCounter++;
		}
	}
}
