package org.stoev.fuzzer;

import org.testng.Assert;

import java.util.Scanner;
import java.util.EnumSet;

import org.stoev.fuzzer.Grammar.GrammarFlags;

final class TestUtil {
	private static final int ITERATIONS = 10;

	private TestUtil() {

	}

	static void assertGenerates(final String grammarString, final String expectedString) {
		Context context = new Context.ContextBuilder().grammar(grammarString).build();

		for (int i = 1; i <= ITERATIONS; i++) {
			Assert.assertEquals(context.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
		}
	}

	static void assertGeneratesSkipWhitespace(final String grammarString, final String expectedString) {
		Context context = new Context.ContextBuilder().grammar(grammarString, EnumSet.of(GrammarFlags.SKIP_WHITESPACE)).build();

		for (int i = 1; i <= ITERATIONS; i++) {
			Assert.assertEquals(context.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
		}
	}

	static void assertToString(final String grammarString, final String expectedString) {
		Grammar grammar = new Grammar(new Scanner(grammarString), EnumSet.noneOf(GrammarFlags.class));
		Assert.assertEquals(grammar.toString(), expectedString, "toString() was produced from the following grammar:\n" + grammarString + "\n");
	}
}
