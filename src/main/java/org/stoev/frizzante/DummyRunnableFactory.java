package org.stoev.frizzante;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyRunnableFactory implements FuzzRunnableFactory {
	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		return new DummyRunnable(runnableManager, threadContext);
	}
}

class DummyRunnable extends FuzzRunnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DummyRunnable.class);

	DummyRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
                super(runnableManager, threadContext);
	}

	@Override
	public final void run() {
		while (executionCounter < threadContext.getGlobalContext().getCount()) {
			Sentence<?> javaSentence = threadContext.generateSentence();
			LOGGER.info(javaSentence.toString());
			executionCounter++;
		}
	}
}
