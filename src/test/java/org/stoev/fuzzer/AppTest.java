package org.stoev.fuzzer;

import org.testng.annotations.Test;

public class AppTest {
	@Test
	public final void testAppSimple() throws Throwable {
		org.stoev.fuzzer.App.main(new String[] {
			"--grammar", "mongodb.grammar"
			, "--runnableFactory", "org.stoev.fuzzer.DummyRunnableFactory"
			, "--duration", "1"
			, "--count", "1"
			, "--range", "10000"
		});
	}
}
