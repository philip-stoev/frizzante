package org.stoev.fuzzer;

import org.testng.Assert;

final class TestUtil {
	private static final int ITERATIONS = 10;

	private TestUtil() {

	}

	static void assertGenerates(final String grammarString, final String expectedString) {
		Grammar grammar = new Grammar(grammarString);
                Context context = new Context.ContextBuilder(grammar).build();

		for (int i = 1; i <= ITERATIONS; i++) {
			Assert.assertEquals(context.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
		}
	}

	static void assertGeneratesEmptySeparator(final String grammarString, final String expectedString) {
		Grammar grammar = new Grammar(grammarString);
                Context context = new Context.ContextBuilder(grammar).separator("").build();

		for (int i = 1; i <= ITERATIONS; i++) {
			Assert.assertEquals(context.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
		}
	}

	static void assertToString(final String grammarString, final String expectedString) {
		Grammar grammar = new Grammar(grammarString);
		Assert.assertEquals(grammar.toString(), expectedString, "toString() was produced from the following grammar:\n" + grammarString + "\n");
	}
}
