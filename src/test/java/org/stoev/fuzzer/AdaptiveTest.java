package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AdaptiveTest {
        public static final int HUNDRED_ITERATIONS = 100;
        public static final int SUCCESS_THRESHOLD = 90;
	public static final double PENALTY = 0.5d;

	@Test
	public final void testPenalize() {
		Grammar g = new Grammar("main: good | bad ;");
                Context c = new Context.ContextBuilder(g).build();

		for (int x = 1; x <= HUNDRED_ITERATIONS; x++) {
			Sentence<String> sentence = new Sentence<String>();
			c.generate(sentence);
			if (sentence.toString().contains("bad")) {
				sentence.fail(PENALTY);
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
}
