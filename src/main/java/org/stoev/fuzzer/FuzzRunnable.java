package org.stoev.fuzzer;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

abstract class FuzzRunnable implements Runnable {

	protected final ThreadContext<?> threadContext;
	protected volatile boolean interrupted = false;
	protected long currentCount = 0;

	FuzzRunnable(final ThreadContext<?> threadContext) {
		this.threadContext = threadContext;
	}

	abstract public void run();

	public void interrupt() {
		interrupted = true;
	}
}
