package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Scanner;
import java.util.EnumSet;

import org.stoev.fuzzer.Grammar.GrammarFlags;

public class AdaptiveTest {
        public static final int HUNDRED_ITERATIONS = 100;
        public static final int SUCCESS_THRESHOLD = 90;
	public static final double HALF_PENALTY = 0.5f;
	public static final double QUARTER_PENALTY = 0.25f;

	@Test
	public final void testAdaptive() {
                Context c = new Context.ContextBuilder().grammar("main: good | bad ;").build();

		for (int x = 1; x <= HUNDRED_ITERATIONS; x++) {
			Sentence<String> sentence = new Sentence<String>();
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
		Grammar g = new Grammar(new Scanner("main: good ;"), EnumSet.noneOf(GrammarFlags.class));
                Context c = new Context.ContextBuilder().grammar(g).build();
		Sentence<String> s = new Sentence<String>();
		c.generate(s);
		s.succeeded(1.0f);
		Assert.assertEquals(g.toString(), "main:1.0 good\n;\n");
	}

	@Test
	public final void testPromotion() {
		Grammar g = new Grammar(new Scanner("main: good ;"), EnumSet.noneOf(GrammarFlags.class));
                Context c = new Context.ContextBuilder().grammar(g).build();
		Sentence<String> s = new Sentence<String>();
		c.generate(s);

		// penalize first before promoting, as the weight is not allowed to exceed the original
		s.failed(HALF_PENALTY);
		s.succeeded(QUARTER_PENALTY);

		Assert.assertEquals(g.toString(), "main:0.6666666666666666 good\n;\n");
	}

	@Test
	public final void testDemotion() {
		Grammar g = new Grammar(new Scanner("main: bad;"), EnumSet.noneOf(GrammarFlags.class));
                Context c = new Context.ContextBuilder().grammar(g).build();
		Sentence<String> s = new Sentence<String>();
		c.generate(s);
		s.failed(HALF_PENALTY);
		Assert.assertEquals(g.toString(), "main:0.5 bad\n;\n");
	}
}
