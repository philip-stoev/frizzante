package org.stoev.fuzzer;

import org.testng.Assert;

final class TestUtil {
	private TestUtil() {

	}

	static void assertGenerates(final String grammarString, final String expectedString) {
		Grammar grammar = new Grammar(grammarString);
                Context context = new Context.ContextBuilder(grammar).build();
		Assert.assertEquals(context.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
	}

	static void assertGeneratesEmptySeparator(final String grammarString, final String expectedString) {
		Grammar grammar = new Grammar(grammarString);
                Context context = new Context.ContextBuilder(grammar).separator("").build();
		Assert.assertEquals(context.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
	}
}
