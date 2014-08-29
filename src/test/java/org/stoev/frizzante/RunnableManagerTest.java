package org.stoev.frizzante;

import org.testng.annotations.Test;
import org.testng.Assert;

import org.stoev.frizzante.GlobalContext.ContextBuilder;

import java.util.concurrent.TimeoutException;

public class RunnableManagerTest {
	@Test(expectedExceptions = IllegalArgumentException.class)
	public final void testBadJavaCode() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: simply_bad_java\n;")
                        .runnableFactory(new JavaBatchRunnableFactory())
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public final void testBadJavaSignature() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: public static void testBadJavaSignature(final String foo) { assert false; }\n;")
                        .runnableFactory(new JavaBatchRunnableFactory())
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public final void testNonpublicJava() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: static void testNonpublicJava() { assert false; }\n;")
                        .runnableFactory(new JavaBatchRunnableFactory())
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test(expectedExceptions = ExecutionException.class)
	public final void testJavaException() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: public static void testJavaException() { assert false; }\n;")
                        .runnableFactory(new JavaBatchRunnableFactory())
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test
	public final void testZeroExecutions() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: public static void run() { assert false; }\n;")
                        .runnableFactory(new JavaBatchRunnableFactory())
                        .count(0)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test
	public final void testSimpleRunnableManager() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: public static void testSimpleRunnableManager() { assert true; }\n;")
                        .runnableFactory(new JavaBatchRunnableFactory())
                        .count(1)
                        .duration(60)
                        .build();

                globalContext.run();
	}

	@Test
	public final void testDuration() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: public static void testSimpleRunnableManager() throws InterruptedException { Thread.sleep(1000); }\n;")
                        .runnableFactory(new JavaBatchRunnableFactory())
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
			.grammar("#option STANDALONE_SEMICOLONS\nmain: public static void testSimpleRunnableManager() throws InterruptedException { Thread.sleep(10000); }\n;")
                        .runnableFactory(new JavaBatchRunnableFactory())
                        .count(100)
                        .duration(1)
                        .build();
                globalContext.run();
	}
}
