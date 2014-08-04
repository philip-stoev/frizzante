package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.HashMap;

import org.stoev.fuzzer.GlobalContext.ContextBuilder;

public class GrammarTest {
	public static final int ITERATIONS = 10;
	public static final int MANY_ITERATIONS = 10000;
	public static final int FOUR = 4;
	public static final int TEN = 10;
	public static final int MANY_ITERATIONS_QUARTER = MANY_ITERATIONS / FOUR;

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testNoMain() {
		GlobalContext<String> g = new ContextBuilder<String>().grammar("").build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);
		c.generateString();
	}

	@Test
	public final void testEmptyMain() {
		GlobalContext<String> g = new ContextBuilder<String>().grammar("main: ;").build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);
		Assert.assertEquals(c.generateString(), "");
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testUnterminatedRule() {
		GlobalContext<String> g = new ContextBuilder<String>().grammar("main: foo").build();
	}

	@Test
	public final void testEmptyProduction1() {
		String grammar = "main:|;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testEmptyProduction2() {
		String grammar = "main: | | || |;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testTightProductions() {
		String grammar = "main:FOO|FOO;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "FOO");
		}
	}

	@Test
	// Check that a production that is split 33/33/33 indeed produces at least remotely fair distribution
	public final void testProductionFairness() {
		String grammar = "main:FOO|BAR|BAZ;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

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

		Assert.assertEquals(map.size(), 3);

		for (Integer value : map.values()) {
			Assert.assertTrue(value > MANY_ITERATIONS_QUARTER);
		}
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testDuplicateRules() {
		String grammar = "main: FOO ;\n main: BAR ;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
	}

	@Test
	public final void testSpecialCharacters() {
		String grammar = "main:\r\n\tFOO\r\n\t|\r\n\tFOO\r\n\t;\r\n\t";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "FOO");
	}

	@Test
	public final void testWeightedProductions() {
		String grammar = "main:80% FOO |20% BAR;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

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
	public final void testSingleWeightedProduction() {
		String grammar = "main:20% FOO ;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "FOO");
		Assert.assertEquals(c.generateString(), "FOO");
		Assert.assertEquals(c.generateString(), "FOO");
	}

	@Test
	public final void testLopsidedWeights() {
		String grammar = "main:99 FOO | BAR ;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

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

	@Test
	public final void testRecursiveWeight() {
		String grammar = "main:95% foo , main |5% foo;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "foo , foo , foo , foo , foo");
	}

	@Test
	public final void testSkipWhitespace() {
		final String[] grammarStrings = {
			"main:foobar;",
			"main: foobar;",
			"main:foobar ;",
			"main:f oobar;",
			"main:fooba r;",
			"main:f o o b a r;",
			"main:x y;\nx:foo;\ny:bar;"
		};

		for (String grammarString: grammarStrings) {
			GlobalContext<String> g = new ContextBuilder<String>().grammar("#option SKIP_WHITESPACE\n" + grammarString).build();
			ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

			Assert.assertEquals(c.generateString(), "foobar", "Problematic string produced by grammar:'" + grammarString + "'");
		}
	}

	@Test
	public final void testWhitespace() {
		final String[] grammarStrings = {
			"main:foo bar;",
			"main:foo bar|foo bar;",
			"main: foo bar;",
			"main: foo bar| foo bar;",
			"main:foo bar ;",
			"main:foo bar |foo bar;",
			"main:x y;\nx:foo;\ny:bar;",
			"main:x y;\nx: foo;\ny: bar;",
			"main:x y;\nx:foo ;\ny:bar ;",
		};

		for (String grammarString: grammarStrings) {
			GlobalContext<String> g = new ContextBuilder<String>().grammar(grammarString).build();
			ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

			Assert.assertEquals(c.generateString(), "foo bar", "Problematic string produced by grammar:'" + grammarString + "'");
		}
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testZeroWeight() {
		GlobalContext<String> g = new ContextBuilder<String>().grammar("main:0 foo;").build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		c.generateString();
	}

	@Test
	public final void testSmallWeights() {
		final String[] grammarStrings = {
			"main:1 foo;",
			"main:1 foo |1 foo;",
			"main:1 foo | foo;",
			"main: foo |1 foo;"
		};

		for (String grammarString: grammarStrings) {
			GlobalContext<String> g = new ContextBuilder<String>().grammar(grammarString).build();
			ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

			for (int x = 1; x < MANY_ITERATIONS; x = x + 1) {
				String generated = c.generateString();
				Assert.assertEquals(generated, "foo", "Problematic string '" + generated + "' produced by grammar '" + grammarString + "'");
			}
		}
	}

	@Test
	public final void testEmptyJava() {
		GlobalContext<String> g = new ContextBuilder<String>().grammar("main: foo ;\n foo.java: {{ }};").build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "");

	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testMalformedJava() {
		String grammar = "main: foo ;\n foo.java: {{ ;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);
		Assert.assertEquals(c.generateString(), "");
	}


	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testInvalidJava() {
		GlobalContext<String> g = new ContextBuilder<String>().grammar("main: foo ;\n foo.java: {{ foo(); }};").build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);
		Assert.assertEquals(c.generateString(), "");
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testJavaException() {
		GlobalContext<String> g = new ContextBuilder<String>().grammar("main: foo ;\n foo.java: {{ int a = 0 ; int b = 2 / a; }};").build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "");
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testEmptyCached() {
		String grammar = "main: main_cached;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);
		c.generateString();
	}

	@Test
	public final void testRuleTree() {
		String grammar = "main: foo , bar ;\n foo: fooA , fooB ;\n bar: barA , barB ;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "fooA , fooB , barA , barB");

	}

	@Test
	public final void testLongSentence() {
		String grammar = "main:999999 X main |1 Y;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertTrue(c.generateString().length() > (TEN * TEN * TEN * TEN));
	}

	@Test
	public final void testMassiveRecursion() {
		String grammar = "main:50% main main |50% Y;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		for (int x = 1; x < MANY_ITERATIONS; x = x + 1) {
			if (c.generateString().length() > (TEN * TEN * TEN)) {
				return;
			}
		}
		Assert.fail("No long sentences produced");
	}

	@Test
	public final void testBackslash() {
		String grammar = "main: foo \\ bar;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "foo \\ bar");
	}

	@Test
	public final void testUnicodeProduction() {
		String grammar = "main: тест ;";
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "тест");
	}
}
