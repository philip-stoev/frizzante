package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class GrammarTest {
	public static final int ITERATIONS = 10;
	public static final int MANY_ITERATIONS = 10000;
	public static final int FOUR = 4;
	public static final int TEN = 10;
	public static final int MANY_ITERATIONS_QUARTER = MANY_ITERATIONS / FOUR;

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testNoMain() {
		Grammar g = new Grammar("");
		Context c = new Context.ContextBuilder(g).build();
		c.generateString();
	}

	@Test
	public final void testEmptyMain() {
		Grammar g = new Grammar("main: ;");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "");
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testUnterminatedRule() {
		Grammar g = new Grammar("main: foo");
	}

	@Test
	public final void testEmptyProduction1() {
		Grammar g = new Grammar("main:|;");
		Context c = new Context.ContextBuilder(g).build();
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testEmptyProduction2() {
		Grammar g = new Grammar("main: | | || |;");
		Context c = new Context.ContextBuilder(g).build();
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testTightProductions() {
		Grammar g = new Grammar("main:FOO|FOO;");
		Context c = new Context.ContextBuilder(g).build();
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "FOO");
		}
	}

	@Test
	// Check that a production that is split 33/33/33 indeed produces at least remotely fair distribution
	public final void testProductionFairness() {
		Grammar g = new Grammar("main:FOO|BAR|BAZ;");
		Context c = new Context.ContextBuilder(g).build();
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
		Grammar g = new Grammar("main: FOO ;\n main: BAR ;");
	}

	@Test
	public final void testSpecialCharacters() {
		Grammar g = new Grammar("main:\r\n\tFOO\r\n\t|\r\n\tFOO\r\n\t;\r\n\t");
		Assert.assertEquals(new Context.ContextBuilder(g).build().generateString(), "FOO");
	}

	@Test
	public final void testWeightedProductions() {
		Grammar g = new Grammar("main:80% FOO |20% BAR;");
		Context c = new Context.ContextBuilder(g).build();
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
		Grammar g = new Grammar("main:20% FOO ;");
		Context c = new Context.ContextBuilder(g).build();

		Assert.assertEquals(c.generateString(), "FOO");
		Assert.assertEquals(c.generateString(), "FOO");
		Assert.assertEquals(c.generateString(), "FOO");
	}

	@Test
	public final void testLopsidedWeights() {
		Grammar g = new Grammar("main:99 FOO | BAR ;");
		Context c = new Context.ContextBuilder(g).build();
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
		Grammar g = new Grammar("main:90% foo , main |10% foo;");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "foo , foo , foo , foo , foo");
		Assert.assertEquals(c.generateString(), "foo , foo");
		Assert.assertEquals(c.generateString(), "foo");
		Assert.assertEquals(c.generateString(), "foo");
	}

	@Test
	public final void testEmptySeparator() {
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
			Grammar g = new Grammar(grammarString);
			Context c = new Context.ContextBuilder(g).separator("").build();
			Assert.assertEquals(c.generateString(), "foobar", "Problematic string produced by grammar:'" + grammarString + "'");
		}
	}

	@Test
	public final void testSeparator() {
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
			Grammar g = new Grammar(grammarString);
			Context c = new Context.ContextBuilder(g).build();
			Assert.assertEquals(c.generateString(), "foo bar", "Problematic string produced by grammar:'" + grammarString + "'");
		}
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testZeroWeight() {
		Grammar g = new Grammar("main:0 foo;");
		Context c = new Context.ContextBuilder(g).build();
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
			Grammar g = new Grammar(grammarString);
			Context c = new Context.ContextBuilder(g).build();
			for (int x = 1; x < MANY_ITERATIONS; x = x + 1) {
				String generated = c.generateString();
				Assert.assertEquals(generated, "foo", "Problematic string '" + generated + "' produced by grammar '" + grammarString + "'");
			}
		}
	}

	@Test
	public final void testEmptyJava() {
		Grammar g = new Grammar("main: foo ;\n foo.java: {{ }};");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "");

	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testMalformedJava() {
		Grammar g = new Grammar("main: foo ;\n foo.java: {{ ;");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "");
	}


	@Test (expectedExceptions = ConfigurationException.class)
	public final void testInvalidJava() {
		Grammar g = new Grammar("main: foo ;\n foo.java: {{ foo(); }};");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "");
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testJavaException() {
		Grammar g = new Grammar("main: foo ;\n foo.java: {{ int a = 0 ; int b = 2 / a; }};");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "");
	}

	@Test
	final void testVisitors() {
		Grammar g = new Grammar("main: foo_visitor bar_visitor;");

		class TestVisitor {
			public void foo(final Context context, final Sentence<String> sentence) {
				sentence.add("foo2");
			}

			public void bar(final Context context, final Sentence<String> sentence) {
				sentence.add("bar2");
			}
		}

		Object v = new TestVisitor();
		Context c = new Context.ContextBuilder(g).visitor(v).separator("").build();

		Assert.assertEquals(c.generateString(), "foo2bar2");
	}

	@Test
	final void testVisitorWithType() {
		Grammar g = new Grammar("main: foo_visitor;");
		class TestObject {

		}

		class TestVisitor {
			public void foo(final Context context, final Sentence<TestObject> sentence) {
				sentence.add(new TestObject());
			}
		}

		Object v = new TestVisitor();
		Context c = new Context.ContextBuilder(g).visitor(v).build();

		Sentence<TestObject> sentence = new Sentence<TestObject>();
		c.generate(sentence);
                Iterator<TestObject> iterator = sentence.iterator();

		Assert.assertTrue(iterator.next() instanceof TestObject);
	}

	@Test
	final void testVisitorCaching() {
		Grammar g = new Grammar("main: cached_visitor cached_visitor;");

		class TestVisitor {
			public void cached(final Context context, final Sentence<String> sentence) {
				sentence.append("cached2");
			}
		}

		Object v = new TestVisitor();
		Context c = new Context.ContextBuilder(g).visitor(v).build();

		Assert.assertEquals(c.generateString(), "cached2 cached2");
		Assert.assertNotNull(c.getCachedVisitor("cached"));
	}

	@Test (expectedExceptions = ConfigurationException.class)
	final void testMissingVisitorMethod() {
                Grammar g = new Grammar("main: missing_visitor;");

                class TestVisitor {

                }

                Object v = new TestVisitor();
                Context c = new Context.ContextBuilder(g).visitor(v).build();
		c.generateString();
	}

	@Test (expectedExceptions = ConfigurationException.class)
	final void testMissingVisitorMethodDefinition() {
		Grammar g = new Grammar("main: missing_visitor;");

		class TestVisitor {
			public int cached(final Context context, final Sentence<String> sentence) {
				return 1;
			}
		}

                Object v = new TestVisitor();
                Context c = new Context.ContextBuilder(g).visitor(v).build();
		c.generateString();
	}

	@Test (expectedExceptions = ConfigurationException.class)
	final void testNonvoidVisitor() {
		Grammar g = new Grammar("main: missing_visitor;");

		class TestVisitor {
			public void cached() {

			}
		}

                Object v = new TestVisitor();
                Context c = new Context.ContextBuilder(g).visitor(v).build();
		c.generateString();
	}

	@Test (expectedExceptions = ConfigurationException.class)
	final void testMissingVisitorClass() {
                Grammar g = new Grammar("main: missing_visitor;");
                Context c = new Context.ContextBuilder(g).build();
		c.generateString();
	}

	@Test
	public final void testEmptyCached() {
		Grammar g = new Grammar("main: main_cached;");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "");
	}

	@Test
	public final void testRuleTree() {
		Grammar g = new Grammar("main: foo , bar ;\n foo: fooA , fooB ;\n bar: barA , barB ;");
                Context c = new Context.ContextBuilder(g).build();
                Assert.assertEquals(c.generateString(), "fooA , fooB , barA , barB");

	}

	@Test
	public final void testLongSentence() {
                Grammar g = new Grammar("main:9999999 X main |1 Y;");
                Context c = new Context.ContextBuilder(g).separator("").build();

		Assert.assertTrue(c.generateString().length() > (TEN * TEN * TEN * TEN));
	}

	@Test
	public final void testMassiveRecursion() {
                Grammar g = new Grammar("main:50% main main |50% Y;");
                Context c = new Context.ContextBuilder(g).separator("").build();

		for (int x = 1; x < MANY_ITERATIONS; x = x + 1) {
			if (c.generateString().length() > (TEN * TEN * TEN)) {
				return;
			}
		}
		Assert.fail("No long sentences produced");
	}

	@Test
	public final void testEscapedPipe() {
		Grammar g = new Grammar("main: foo \\| bar | foo \\| bar;");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "foo | bar");

	}

	@Test
	public final void testBackslash() {
		Grammar g = new Grammar("main: foo \\ bar;");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "foo \\ bar");
	}

	@Test
	public final void testUnicodeProduction() {
		Grammar g = new Grammar("main: тест ;");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "тест");
	}
}
