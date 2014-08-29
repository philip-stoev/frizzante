package org.stoev.frizzante;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Scanner;

public class AdaptiveTest {
	public static final int HUNDRED_ITERATIONS = 100;
	public static final int SUCCESS_THRESHOLD = 90;
	public static final double HALF_PENALTY = 0.5f;
	public static final double QUARTER_PENALTY = 0.25f;

	@Test
	public final void testAdaptive() {
		GlobalContext<String> g = new GlobalContext.ContextBuilder<String>().grammar("main: good | bad ;").build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		for (int x = 1; x <= HUNDRED_ITERATIONS; x++) {
			Sentence<String> sentence = c.newSentence();
			c.generate(sentence);
			if (sentence.toString().contains("bad")) {
				sentence.failed(HALF_PENALTY);
			}
		}

		int goodGenerations = 0;

		for (int x = 1; x <= HUNDRED_ITERATIONS; x++) {
			if (c.generateString().contains("good")) {
				goodGenerations++;
			}
		}

		Assert.assertTrue(goodGenerations > SUCCESS_THRESHOLD);
	}

	@Test
	public final void testPromotionLimit() {
		Grammar<String> grammar = new Grammar<String>(new Scanner("main: good ;"));
		GlobalContext<String> g = new GlobalContext.ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Sentence<String> s = c.newSentence();
		c.generate(s);
		s.succeeded(1.0f);
		Assert.assertEquals(grammar.getGrammarString(), "main:1.0 good\n;\n");
	}

	@Test
	public final void testPromotion() {
		Grammar<String> grammar = new Grammar<String>(new Scanner("main: good ;"));
		GlobalContext<String> g = new GlobalContext.ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Sentence<String> s = c.newSentence();
		c.generate(s);

		// penalize first before promoting, as the weight is not allowed to exceed the original
		s.failed(HALF_PENALTY);
		s.succeeded(QUARTER_PENALTY);

		Assert.assertEquals(grammar.getGrammarString(), "main:0.6666666666666666 good\n;\n");
	}

	@Test
	public final void testDemotion() {
		Grammar<String> grammar = new Grammar<String>(new Scanner("main: bad;"));
		GlobalContext<String> g = new GlobalContext.ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Sentence<String> s = c.newSentence();
		c.generate(s);
		s.failed(HALF_PENALTY);
		Assert.assertEquals(grammar.getGrammarString(), "main:0.5 bad\n;\n");
	}
}
