package org.stoev.fuzzer;

import org.testng.annotations.Test;
import org.testng.Assert;

import org.stoev.fuzzer.Grammar.GrammarFlags;
import org.stoev.fuzzer.GlobalContext.ContextBuilder;

import java.util.EnumSet;

import java.util.concurrent.TimeoutException;

public class RunnableManagerTest {
	@Test(expectedExceptions = IllegalArgumentException.class)
	public final void testBadJavaCode() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("main: simply_bad_java\n;", EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
                        .runnable(JavaBatchRunnable.class)
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public final void testBadJavaSignature() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("main: public static void testBadJavaSignature() { assert false; }\n;", EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
                        .runnable(JavaBatchRunnable.class)
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public final void testNonpublicJava() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("main: static void testNonpublicJava(ThreadContext<String> threadContext) { assert false; }\n;", EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
                        .runnable(JavaBatchRunnable.class)
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test(expectedExceptions = ExecutionException.class)
	public final void testJavaException() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("main: public static void testJavaException(ThreadContext<String> threadContext) { assert false; }\n;", EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
                        .runnable(JavaBatchRunnable.class)
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test
	public final void testZeroExecutions() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("main: public static void run(ThreadContext<String> threadContext) { assert false; }\n;", EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
                        .runnable(JavaBatchRunnable.class)
                        .count(0)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test
	public final void testSimpleRunnableManager() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("main: public static void testSimpleRunnableManager(ThreadContext<String> threadContext) { assert true; }\n;", EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
                        .runnable(JavaBatchRunnable.class)
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test
	public final void testDuration() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("main: public static void testSimpleRunnableManager(ThreadContext<String> threadContext) throws InterruptedException { Thread.sleep(1000); }\n;", EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
                        .runnable(JavaBatchRunnable.class)
                        .count(100)
                        .duration(1)
                        .build();

		long start = System.currentTimeMillis();
                globalContext.run();
		long end = System.currentTimeMillis();
		long duration = end - start;

		Assert.assertTrue(duration >= 1000);
		Assert.assertTrue(duration <= 1500);
	}

	@Test (expectedExceptions = TimeoutException.class)
	public final void testTimeout() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("main: public static void testSimpleRunnableManager(ThreadContext<String> threadContext) throws InterruptedException { Thread.sleep(10000); }\n;", EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
                        .runnable(JavaBatchRunnable.class)
                        .count(100)
                        .duration(1)
                        .build();
                globalContext.run();
	}
}
