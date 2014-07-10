package org.stoev.fuzzer;

import java.io.IOException;

public abstract class FuzzRunnableFactory {
	public abstract FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) throws IOException;
}
