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
		Context<String> context = new Context.ContextBuilder<String>().grammar(grammarString).build();

		for (int i = 1; i <= ITERATIONS; i++) {
			Assert.assertEquals(context.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
		}
	}

	static void assertGeneratesSkipWhitespace(final String grammarString, final String expectedString) {
		Context<String> context = new Context.ContextBuilder<String>().grammar(grammarString, EnumSet.of(GrammarFlags.SKIP_WHITESPACE)).build();

		for (int i = 1; i <= ITERATIONS; i++) {
			Assert.assertEquals(context.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
		}
	}

	static void assertToString(final String grammarString, final String expectedString) {
		Grammar<String> grammar = new Grammar<String>(new Scanner(grammarString), EnumSet.noneOf(GrammarFlags.class));
		Assert.assertEquals(grammar.toString(), expectedString, "toString() was produced from the following grammar:\n" + grammarString + "\n");
	}

	static void assertShortestConstantSentence(final String grammarString, final String expectedShortestConstantString) {
                Context<String> context = new Context.ContextBuilder<String>().grammar(grammarString).build();
                Sentence<String> sentence = context.newSentence();
                context.generate(sentence);

                Grammar<String> grammar = context.getGrammar();
                GrammarRule<String> rule = (GrammarRule<String>) grammar.getRule("main");
                Sentence<String> constantSentence = rule.getShortestConstantSentence();

                Assert.assertEquals(constantSentence.toString(), expectedShortestConstantString);
        }
}
