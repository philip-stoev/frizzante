package org.stoev.frizzante;

import org.testng.annotations.Test;

public class AppTest {
	@Test
	public final void testAppSimple() throws Throwable {
		org.stoev.frizzante.App.main(new String[] {
			"--grammar", "mongodb.grammar"
			, "--runnableFactory", "org.stoev.frizzante.DummyRunnableFactory"
			, "--duration", "1"
			, "--count", "1"
			, "--range", "10000"
		});
	}
}
