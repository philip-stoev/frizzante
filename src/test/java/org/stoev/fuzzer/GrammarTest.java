package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

import org.stoev.fuzzer.Grammar.GrammarFlags;
import org.stoev.fuzzer.Context.ContextBuilder;

public class GrammarTest {
	public static final int ITERATIONS = 10;
	public static final int MANY_ITERATIONS = 10000;
	public static final int FOUR = 4;
	public static final int TEN = 10;
	public static final int MANY_ITERATIONS_QUARTER = MANY_ITERATIONS / FOUR;

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testNoMain() {
		Context c = new ContextBuilder().grammar("").build();
		c.generateString();
	}

	@Test
	public final void testEmptyMain() {
		Context c = new ContextBuilder().grammar("main: ;").build();
		Assert.assertEquals(c.generateString(), "");
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testUnterminatedRule() {
		Context c = new ContextBuilder().grammar("main: foo").build();
	}

	@Test
	public final void testEmptyProduction1() {
		String g = "main:|;";
		Context c = new ContextBuilder().grammar(g).build();
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testEmptyProduction2() {
		String g = "main: | | || |;";
		Context c = new ContextBuilder().grammar(g).build();
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testTightProductions() {
		String g = "main:FOO|FOO;";
		Context c = new ContextBuilder().grammar(g).build();
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "FOO");
		}
	}

	@Test
	// Check that a production that is split 33/33/33 indeed produces at least remotely fair distribution
	public final void testProductionFairness() {
		String g = "main:FOO|BAR|BAZ;";
		Context c = new ContextBuilder().grammar(g).build();
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
	public final void testDuplicateRules() {
		String g = "main: FOO ;\n main: BAR ;";
		Context c = new ContextBuilder().grammar(g).build();
	}

	@Test
	public final void testSpecialCharacters() {
		String g = "main:\r\n\tFOO\r\n\t|\r\n\tFOO\r\n\t;\r\n\t";
		Assert.assertEquals(new ContextBuilder().grammar(g).build().generateString(), "FOO");
	}

	@Test
	public final void testWeightedProductions() {
		String g = "main:80% FOO |20% BAR;";
		Context c = new ContextBuilder().grammar(g).build();
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
		String g = "main:20% FOO ;";
		Context c = new ContextBuilder().grammar(g).build();

		Assert.assertEquals(c.generateString(), "FOO");
		Assert.assertEquals(c.generateString(), "FOO");
		Assert.assertEquals(c.generateString(), "FOO");
	}

	@Test
	public final void testLopsidedWeights() {
		String g = "main:99 FOO | BAR ;";
		Context c = new ContextBuilder().grammar(g).build();
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
		String g = "main:90% foo , main |10% foo;";
		Context c = new ContextBuilder().grammar(g).build();
		Assert.assertEquals(c.generateString(), "foo , foo , foo , foo , foo");
		Assert.assertEquals(c.generateString(), "foo , foo");
		Assert.assertEquals(c.generateString(), "foo");
		Assert.assertEquals(c.generateString(), "foo");
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
			Context c = new ContextBuilder().grammar(grammarString, EnumSet.of(GrammarFlags.SKIP_WHITESPACE)).build();
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
			Context c = new ContextBuilder().grammar(grammarString).build();
			Assert.assertEquals(c.generateString(), "foo bar", "Problematic string produced by grammar:'" + grammarString + "'");
		}
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testZeroWeight() {
		Context c = new ContextBuilder().grammar("main:0 foo;").build();
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
			Context c = new ContextBuilder().grammar(grammarString).build();
			for (int x = 1; x < MANY_ITERATIONS; x = x + 1) {
				String generated = c.generateString();
				Assert.assertEquals(generated, "foo", "Problematic string '" + generated + "' produced by grammar '" + grammarString + "'");
			}
		}
	}

	@Test
	public final void testEmptyJava() {
		Context c = new ContextBuilder().grammar("main: foo ;\n foo.java: {{ }};").build();
		Assert.assertEquals(c.generateString(), "");

	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testMalformedJava() {
		String g = "main: foo ;\n foo.java: {{ ;";
		Context c = new ContextBuilder().grammar(g).build();
		Assert.assertEquals(c.generateString(), "");
	}


	@Test (expectedExceptions = ConfigurationException.class)
	public final void testInvalidJava() {
		Context c = new ContextBuilder().grammar("main: foo ;\n foo.java: {{ foo(); }};").build();
		Assert.assertEquals(c.generateString(), "");
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testJavaException() {
		Context c = new ContextBuilder().grammar("main: foo ;\n foo.java: {{ int a = 0 ; int b = 2 / a; }};").build();
		Assert.assertEquals(c.generateString(), "");
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testEmptyCached() {
		String g = "main: main_cached;";
		Context c = new ContextBuilder().grammar(g).build();
		c.generateString();
	}

	@Test
	public final void testRuleTree() {
		String g = "main: foo , bar ;\n foo: fooA , fooB ;\n bar: barA , barB ;";
		Context c = new ContextBuilder().grammar(g).build();
		Assert.assertEquals(c.generateString(), "fooA , fooB , barA , barB");

	}

	@Test
	public final void testLongSentence() {
		String g = "main:9999999 X main |1 Y;";
		Context c = new ContextBuilder().grammar(g).build();

		Assert.assertTrue(c.generateString().length() > (TEN * TEN * TEN * TEN));
	}

	@Test
	public final void testMassiveRecursion() {
		String g = "main:50% main main |50% Y;";
		Context c = new ContextBuilder().grammar(g).build();

		for (int x = 1; x < MANY_ITERATIONS; x = x + 1) {
			if (c.generateString().length() > (TEN * TEN * TEN)) {
				return;
			}
		}
		Assert.fail("No long sentences produced");
	}

	@Test
	public final void testBackslash() {
		String g = "main: foo \\ bar;";
		Context c = new ContextBuilder().grammar(g).build();
		Assert.assertEquals(c.generateString(), "foo \\ bar");
	}

	@Test
	public final void testUnicodeProduction() {
		String g = "main: тест ;";
		Context c = new ContextBuilder().grammar(g).build();
		Assert.assertEquals(c.generateString(), "тест");
	}
}
