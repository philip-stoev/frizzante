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
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar(grammarString).build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		for (int i = 1; i <= ITERATIONS; i++) {
			Assert.assertEquals(threadContext.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
		}
	}

	static void assertGeneratesSkipWhitespace(final String grammarString, final String expectedString) {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar(grammarString, EnumSet.of(GrammarFlags.SKIP_WHITESPACE)).build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		for (int i = 1; i <= ITERATIONS; i++) {
			Assert.assertEquals(threadContext.generateString(), expectedString, "String was generated using the following grammar:\n" + grammarString + "\n");
		}
	}

	static void assertToString(final String grammarString, final String expectedString) {
		Grammar<String> grammar = new Grammar<String>(new Scanner(grammarString), EnumSet.noneOf(GrammarFlags.class));
		Assert.assertEquals(grammar.getGrammarString(), expectedString, "toString() was produced from the following grammar:\n" + grammarString + "\n");
	}

	static void assertShortestConstantSentence(final String grammarString, final String expectedShortestConstantString) {
                GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar(grammarString).build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

                Sentence<String> sentence = threadContext.newSentence();
                threadContext.generate(sentence);

                Grammar<String> grammar = globalContext.getGrammar();
                GrammarRule<String> rule = (GrammarRule<String>) grammar.getRule("main");
                Sentence<String> constantSentence = rule.getShortestConstantSentence();

                Assert.assertEquals(constantSentence.toString(), expectedShortestConstantString);
        }
}
