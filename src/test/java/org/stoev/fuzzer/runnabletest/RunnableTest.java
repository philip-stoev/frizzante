package org.stoev.fuzzer.test.runnable;

import org.testng.annotations.Test;
import org.testng.Assert;

import org.stoev.fuzzer.GlobalContext;
import org.stoev.fuzzer.ThreadContext;
import org.stoev.fuzzer.RunnableManager;
import org.stoev.fuzzer.JavaBatchRunnable;
import org.stoev.fuzzer.FuzzRunnable;
import org.stoev.fuzzer.FuzzRunnableFactory;

import org.stoev.fuzzer.ExecutionException;

public class RunnableTest {
	@Test
	public final void testRunnable() throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain:public static void run() { Assert.assertTrue(boolean_value); }\n;\nboolean_value:\ntrue|false\n;")
			.runnableFactory(new TestRunnableFactory())
			.count(100)
			.threads(1)
			.duration(60)
			.build();

		try {
			globalContext.run();
		} catch (ExecutionException e) {
			Assert.assertEquals(e.getCause().getClass(), AssertionError.class);
			return;
		}

		Assert.fail();
	}
}

class TestRunnableFactory extends FuzzRunnableFactory {
	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		return new TestRunnable(runnableManager, threadContext);
	}
}

class TestRunnable extends JavaBatchRunnable {
	public TestRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		super(runnableManager, threadContext);
	}

	@Override
	protected String[] getHeaders() {
		return new String[] {
			"import org.testng.Assert"
		};
	};
}
