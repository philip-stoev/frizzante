package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
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
		Context c = new Context.ContextBuilder(g).build();
		c.generateString();
	}

	@Test
	public final void testEmptyMain() throws IOException {
		Grammar g = new Grammar("main: ;");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "");
	}

	@Test
	public final void testEmptyProduction1() throws IOException {
		Grammar g = new Grammar("main:|;");
		Context c = new Context.ContextBuilder(g).build();
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testEmptyProduction2() throws IOException {
		Grammar g = new Grammar("main: | | || |;");
		Context c = new Context.ContextBuilder(g).build();
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "");
		}
	}

	@Test
	public final void testTightProductions() throws IOException {
		Grammar g = new Grammar("main:FOO|FOO;");
		Context c = new Context.ContextBuilder(g).build();
		for (int x = 1; x < ITERATIONS; x = x + 1) {
			Assert.assertEquals(c.generateString(), "FOO");
		}
	}

	@Test
	// Check that a production that is split 33/33/33 indeed produces at least remotely fair distribution
	public final void testProductionFairness() throws IOException {
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
	public final void testDuplicateRules() throws IOException {
		Grammar g = new Grammar("main: FOO ; main: BAR ;");
	}

	@Test
	public final void testSpecialCharacters() throws IOException {
		Grammar g = new Grammar("main:\r\n\tFOO\r\n\t|\r\n\tFOO\r\n\t;\r\n\t");
		Assert.assertEquals(new Context.ContextBuilder(g).build().generateString(), "FOO");
	}

	@Test
	public final void testWeightedProductions() throws IOException {
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
	public final void testSingleWeightedProduction() throws IOException {
		Grammar g = new Grammar("main:20% FOO ;");
		Context c = new Context.ContextBuilder(g).build();

		Assert.assertEquals(c.generateString(), "FOO");
		Assert.assertEquals(c.generateString(), "FOO");
		Assert.assertEquals(c.generateString(), "FOO");
	}

	@Test
	public final void testLopsidedWeights() throws IOException {
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
	public final void testEmptySeparator() throws IOException {
		final String[] grammarStrings = {
			"main:foobar;",
			"main: foobar;",
			"main:foobar ;",
			"main:f oobar;",
			"main:fooba r;",
			"main:f o o b a r;",
			"main:x y;x:foo;y:bar;",
//			"main:foo\t\r\nbar;",
			"main:\t\r\nfoobar;",
			"main:foobar\t\r\n;"
		};

		for (String grammarString: grammarStrings) {
			Grammar g = new Grammar(grammarString);
			Context c = new Context.ContextBuilder(g).separator("").build();
			Assert.assertEquals(c.generateString(), "foobar", "Problematic string produced by grammar:'" + grammarString + "'");
		}
	}

	@Test
	public final void testSeparator() throws IOException {
		final String[] grammarStrings = {
			"main:foo bar;",
			"main:foo bar|foo bar;",
			"main: foo bar;",
			"main: foo bar| foo bar;",
			"main:foo bar ;",
			"main:foo bar |foo bar;",
			"main:x y;x:foo;y:bar;",
			"main:x y;x: foo;y: bar;",
			"main:x y;x:foo ;y:bar ;",
		};

		for (String grammarString: grammarStrings) {
			Grammar g = new Grammar(grammarString);
			Context c = new Context.ContextBuilder(g).build();
			Assert.assertEquals(c.generateString(), "foo bar", "Problematic string produced by grammar:'" + grammarString + "'");
		}
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testZeroWeight() throws IOException {
		Grammar g = new Grammar("main:0 foo;");
		Context c = new Context.ContextBuilder(g).build();
		c.generateString();
	}

	@Test
	public final void testSmallWeights() throws IOException {
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
	public final void testEmptyJava() throws IOException {
		Grammar g = new Grammar("main: foo ; foo.java: {{ }};");
		Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "");

	}

	@Test
	final void testVisitors() throws IOException {
		Grammar g = new Grammar("main: foo.visitor bar.visitor;");

		class TestVisitor {
			public void foo(final Context context, final Sentence<?> sentence) {
				sentence.add("foo2");
			}

			public void bar(final Context context, final Sentence<?> sentence) {
				sentence.add("bar2");
			}
		}

		Object v = new TestVisitor();
		Context c = new Context.ContextBuilder(g).visitor(v).separator("").build();

		Assert.assertEquals(c.generateString(), "foo2bar2");
	}

	@Test
	final void testVisitorWithType() throws IOException {
		Grammar g = new Grammar("main: foo.visitor;");
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
                g.generate(c, sentence);
                Iterator<TestObject> iterator = sentence.iterator();

		Assert.assertTrue(iterator.next() instanceof TestObject);
	}
}
