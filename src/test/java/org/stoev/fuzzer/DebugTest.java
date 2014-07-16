package org.stoev.fuzzer;

import org.testng.annotations.Test;

import org.stoev.fuzzer.GlobalContext.ContextBuilder;

public class DebugTest {
	@Test(expectedExceptions = IllegalArgumentException.class)
	public final void testVisitorSimple() {
		GlobalContext<String> g = new ContextBuilder<String>().grammar("main: foo bar baz").build();
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testDuplicateRules() {
		GlobalContext<String> g = new ContextBuilder<String>().grammar("main: foo;\nfoo: foo1;\nfoo: foo2;\n").build();
	}
}
