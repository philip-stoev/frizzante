package org.stoev.frizzante.test.runnable;

import org.testng.annotations.Test;
import org.testng.Assert;

import org.stoev.frizzante.GlobalContext;
import org.stoev.frizzante.ThreadContext;
import org.stoev.frizzante.RunnableManager;
import org.stoev.frizzante.JavaBatchRunnable;
import org.stoev.frizzante.FuzzRunnable;
import org.stoev.frizzante.FuzzRunnableFactory;

import org.stoev.frizzante.ExecutionException;

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

class TestRunnableFactory implements FuzzRunnableFactory {
	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		return new TestRunnable(runnableManager, threadContext);
	}
}

class TestRunnable extends JavaBatchRunnable {
	TestRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		super(runnableManager, threadContext);
	}

	@Override
	protected String[] getHeaders() {
		return new String[] {
			"import org.testng.Assert"
		};
	};
}
