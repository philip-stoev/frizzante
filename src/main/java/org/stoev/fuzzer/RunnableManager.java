package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;

import java.util.Timer;
import java.util.TimerTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RunnableManager {
	final int threadCount;
	final long duration;

	final GlobalContext<?> globalContext;
	final Class<? extends FuzzRunnable> runnableClass;
	final Constructor<? extends FuzzRunnable> constructor;
	final List<FuzzRunnable> runnables = new ArrayList<FuzzRunnable>();
	final List<Thread> threads = new ArrayList<Thread>();

	final Timer timer = new Timer(true);

	RunnableManager(final GlobalContext<?> globalContext) {
		this.globalContext = globalContext;
		this.runnableClass = globalContext.getRunnableClass();

		try {
			this.constructor = runnableClass.getConstructor(ThreadContext.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}

		this.threadCount = globalContext.getThreadCount();
		this.duration = globalContext.getDuration();
	}

	void run() {
		System.out.println("Starting " + threadCount + " threads...");

		for (int i = 1; i <= threadCount; i++) {
			ThreadContext<?> threadContext = globalContext.newThreadContext(i);

			try {
				FuzzRunnable runnable = constructor.newInstance(threadContext);
				runnables.add(runnable);

	                        Thread thread = new Thread(runnable);
				threads.add(thread);

	                        thread.start();
			} catch (InstantiationException e) {
				assert false;
			} catch (IllegalAccessException e) {
				assert false;
			} catch (InvocationTargetException e) {
				assert false;
			}
		}

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

		System.out.println("Waiting for threads to complete...");

		for (Thread thread : threads) {
			try {
				thread.join();
			} catch(InterruptedException e) {
				assert false;
			}
		}

		System.out.println("Execution completed.");
	}
}
