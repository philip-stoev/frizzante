package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;

import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.TimeoutException;

public final class RunnableManager {
	private final int threadCount;
	private final long duration;

	private final GlobalContext<?> globalContext;

	private final List<FuzzRunnable> runnables = new ArrayList<FuzzRunnable>();
	private final List<Thread> threads = new ArrayList<Thread>();

	private final Timer timer = new Timer(true);
	private TimeoutException timeoutException;

	RunnableManager(final GlobalContext<?> globalContext) {
		assert globalContext != null;

		this.globalContext = globalContext;

		this.threadCount = globalContext.getThreadCount();
		this.duration = globalContext.getDuration();
	}

	void run() throws TimeoutException, ExecutionException {
		scheduleTermination();
		startThreads();
		waitForThreads();
		reapExceptions();

		System.out.println("Execution completed successfully.");
	}

	void startThreads() {
		System.out.println("Starting " + threadCount + " threads.");

		for (int i = 1; i <= threadCount; i++) {
			ThreadContext<?> threadContext = ThreadContext.newThreadContext(globalContext, i);

			FuzzRunnable runnable = globalContext.getRunnableFactory().newRunnable(this, threadContext);
			runnables.add(runnable);

                        Thread thread = new Thread(runnable);
			threads.add(thread);
                        thread.start();
		}
	}

	void scheduleTermination() {
		System.out.println("Scheduling termination in " + duration + " seconds.");

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Desired execution duration reached.");
                                for (FuzzRunnable runnable: runnables) {
                                        runnable.interrupt();
                                }
                        }
                }, duration * 1000L);

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.err.println("Timeout.");
				timeoutException = new TimeoutException("Execution took twice longer than desired.");
				killAll();
                        }
                }, duration * 2 * 1000L);
	}

	void reapExceptions() throws TimeoutException, ExecutionException {
		if (timeoutException != null) {
			throw timeoutException;
		}

		for (FuzzRunnable runnable: runnables) {
			if (runnable.getRuntimeException() != null) {
				throw runnable.getRuntimeException();
			}

			if (runnable.getExecutionException() != null) {
				throw runnable.getExecutionException();
			}
		}
	}

	void killAll() {
		System.out.println("Terminating all threads.");

		timer.cancel();

		for (FuzzRunnable runnable: runnables) {
			runnable.interrupt();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			assert false;
		}

		for (Thread thread: threads) {
                	thread.interrupt();
		}
	}

	void waitForThreads() {
		System.out.println("Waiting for threads to complete...");

		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				assert false;
			}
		}

		timer.cancel();
	}
}
