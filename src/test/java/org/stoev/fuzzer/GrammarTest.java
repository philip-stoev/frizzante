package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

public class GrammarTest {
	public static final int ITERATIONS = 10;
	public static final int MANY_ITERATIONS = 10000;
	public static final int FOUR = 4;
	public static final int MANY_ITERATIONS_QUARTER = MANY_ITERATIONS / FOUR;

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testNoMain() throws IOException {
		Grammar g = new Grammar("");
		Context c = new Context(g);
		c.generateString();
	}

	@Test
	public final void testEmptyMain() throws IOException {
		Grammar g = new Grammar("main: ;");
		Context c = new Context(g);
		Assert.assertEquals(c.generateString(), "");
	}

	@Test
	public final void testEmptyProduction1() throws IOException {
		Grammar g = new Grammar("main:|;");
		Context c = new Context(g);
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testEmptyProduction2() throws IOException {
		Grammar g = new Grammar("main: | | || |;");
		Context c = new Context(g);
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testTightProductions() throws IOException {
		Grammar g = new Grammar("main:FOO|FOO;");
		Context c = new Context(g);
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "FOO");
		}
	}

	@Test
	// Check that a production that is split 33/33/33 indeed produces at least remotely fair distribution
	public final void testProductionFairness() throws IOException {
		Grammar g = new Grammar("main:FOO|BAR|BAZ;");
		Context c = new Context(g);
		Map<String, Integer> map = new HashMap<String, Integer>();

		for (int x = 1; x < MANY_ITERATIONS; x = x + 1) {
			String generated = c.generateString();
			Assert.assertNotEquals(generated, "");

			if (map.containsKey(generated)) {
				map.put(generated, map.get(generated) + 1);
			} else {
				map.put(generated, 0);
			}
		}

		// silly workaround for checkstyle magic numbers warning
		Assert.assertEquals(map.size(), 1 + 1 + 1);

		for (Integer value : map.values()) {
			Assert.assertTrue(value > MANY_ITERATIONS_QUARTER);
		}
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testDuplicateRules() throws IOException {
		Grammar g = new Grammar("main: FOO ; main: BAR ;");
	}

	@Test
	public final void testSpecialCharacters() throws IOException {
		Grammar g = new Grammar("main:\r\n\tFOO\r\n\t|\r\n\tFOO\r\n\t;\r\n\t");
		Assert.assertEquals(new Context(g).generateString(), "FOO");
	}

	@Test
	public final void testWeightedProductions() throws IOException {
		Grammar g = new Grammar("main:80% FOO |20% BAR;");
		Context c = new Context(g);
		Map<String, Integer> map = new HashMap<String, Integer>();

		for (int x = 1; x < MANY_ITERATIONS; x = x + 1) {
			String generated = c.generateString();
			Assert.assertNotEquals(generated, "");

			if (map.containsKey(generated)) {
				map.put(generated, map.get(generated) + 1);
			} else {
				map.put(generated, 0);
			}
		}

		Assert.assertEquals(map.size(), 2);
		Assert.assertTrue(map.get("FOO") > MANY_ITERATIONS_QUARTER);
		Assert.assertTrue(map.get("BAR") < MANY_ITERATIONS_QUARTER);
	}

	@Test
	public final void testSingleWeightedProduction() throws IOException {
		Grammar g = new Grammar("main:20% FOO ;");
		Context c = new Context(g);

		Assert.assertEquals(new Context(g).generateString(), "FOO");
		Assert.assertEquals(new Context(g).generateString(), "FOO");
		Assert.assertEquals(new Context(g).generateString(), "FOO");
	}

	@Test
	public final void testLopsidedWeights() throws IOException {
		Grammar g = new Grammar("main:99 FOO | BAR ;");
		Context c = new Context(g);
		Map<String, Integer> map = new HashMap<String, Integer>();

		for (int x = 1; x < MANY_ITERATIONS; x = x + 1) {
			String generated = c.generateString();
			Assert.assertNotEquals(generated, "");

			if (map.containsKey(generated)) {
				map.put(generated, map.get(generated) + 1);
			} else {
				map.put(generated, 0);
			}
		}

		Assert.assertEquals(map.size(), 2);
	}

}
