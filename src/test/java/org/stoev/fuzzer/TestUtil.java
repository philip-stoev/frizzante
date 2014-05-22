package org.stoev.fuzzer;

import org.testng.Assert;

import org.stoev.fuzzer.Grammar.GrammarFlags;

import java.util.EnumSet;

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

	static void assertGeneratesSkipWhitespace(final String grammarString, final String expectedString) {
		Grammar grammar = new Grammar(grammarString, EnumSet.of(GrammarFlags.SKIP_WHITESPACE));
                Context context = new Context.ContextBuilder(grammar).build();

		for (int i = 1; i <= ITERATIONS; i++) {
			Assert.assertEquals(context.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
		}
	}

	static void assertToString(final String grammarString, final String expectedString) {
		Grammar grammar = new Grammar(grammarString);
		Assert.assertEquals(grammar.toString(), expectedString, "toString() was produced from the following grammar:\n" + grammarString + "\n");
	}
}
