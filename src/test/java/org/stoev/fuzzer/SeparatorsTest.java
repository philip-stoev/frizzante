package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SeparatorsTest {
	@Test
	public final void testTightSpacing() {
		TestUtil.assertGenerates("main:foo;", "foo");
		TestUtil.assertGenerates("main:foo|foo;", "foo");
	}

	@Test
	public final void testLeadingGrammarWhitespace() {
		TestUtil.assertGenerates(" main: foo;", "foo");
	}

	@Test
	public final void testTrailingGrammarWhitespace() {
		TestUtil.assertGenerates("main: foo; ", "foo");
	}

	@Test
	public final void testLeadingRuleNameWhitespace() {
		TestUtil.assertGenerates("main: foo;\n foo: foo2;", "foo2");
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testTrailingRuleNameWhitespace() {
		TestUtil.assertGenerates("main: foo;\nfoo : foo2;", "foo2");
	}

	@Test
	public final void testTrailingRuleWhitespace() {
		TestUtil.assertGenerates("main: foo; ", "foo");
		TestUtil.assertGenerates("main: foo;  ", "foo");
		TestUtil.assertGenerates("main: foo;\n", "foo");
		TestUtil.assertGenerates("main: foo;\n\n", "foo");
		TestUtil.assertGenerates("main: foo;\t\t", "foo");
		TestUtil.assertGenerates("main: foo;\r\n\t", "foo");
		TestUtil.assertGenerates("main: foo;\n\nfoo: foo2;", "foo2");
	}

	@Test
	public final void testSpaces() {
		TestUtil.assertGenerates("main: foo;", "foo");
		TestUtil.assertGenerates("main:  foo;", "foo");
		TestUtil.assertGenerates("main:foo ;", "foo");
		TestUtil.assertGenerates("main:foo  ;", "foo");
		TestUtil.assertGenerates("main:foo bar;", "foo bar");
		TestUtil.assertGenerates("main:foo bar ;", "foo bar");
		TestUtil.assertGenerates("main: foo bar;", "foo bar");
		TestUtil.assertGenerates("main: foo\tbar;", "foo\tbar");
		TestUtil.assertGenerates("main: foo\nbar;", "foo\nbar");
	}

	@Test
	public final void testCurlyBraces() {
		TestUtil.assertGenerates("main:{};", "{}");
		TestUtil.assertGenerates("main: {};", "{}");
		TestUtil.assertGenerates("main:{} ;", "{}");

		TestUtil.assertGenerates("main:foo{};", "foo{}");
		TestUtil.assertGenerates("main: foo{};", "foo{}");
		TestUtil.assertGenerates("main:foo{} ;", "foo{}");
	}

	@Test
	public final void testDotLiteral() {
		TestUtil.assertGenerates("main:.foo;", ".foo");
		TestUtil.assertGenerates("main:foo.;", "foo.");
		TestUtil.assertGenerates("main:foo.bar;", "foo.bar");
	}

	@Test
	public final void testDotRules() {
		TestUtil.assertGenerates("main:foo.bar;\nfoo:foo2;\nbar:bar2;", "foo2.bar2");
	}

	@Test
	public final void testPipe() {
		TestUtil.assertGenerates("main:foo|foo;", "foo");
		TestUtil.assertGenerates("main:foo |foo;", "foo");
		TestUtil.assertGenerates("main:foo| foo;", "foo");
		TestUtil.assertGenerates("main:foo|\nfoo;", "foo");
		TestUtil.assertGenerates("main:foo|\n\nfoo;", "foo");
		TestUtil.assertGenerates("main:foo\n|foo;", "foo");
		TestUtil.assertGenerates("main:foo\n\n|foo;", "foo");
	}

	@Test
	public final void testMultiplePipe() {
		TestUtil.assertToString("main:|;", "main:1.0 \n|1.0 \n;\n");
		TestUtil.assertToString("main: |;", "main:1.0 \n|1.0 \n;\n");
		TestUtil.assertToString("main:| ;", "main:1.0 \n|1.0 \n;\n");
		TestUtil.assertToString("main:||;", "main:1.0 \n|1.0 \n|1.0 \n;\n");

		TestUtil.assertToString("main:||;", "main:1.0 \n|1.0 \n|1.0 \n;\n");
		TestUtil.assertToString("main: ||;", "main:1.0 \n|1.0 \n|1.0 \n;\n");
		TestUtil.assertToString("main:| |;", "main:1.0 \n|1.0 \n|1.0 \n;\n");
		TestUtil.assertToString("main:|| ;", "main:1.0 \n|1.0 \n|1.0 \n;\n");

		TestUtil.assertToString("main:|||;", "main:1.0 \n|1.0 \n|1.0 \n|1.0 \n;\n");
	}

	@Test
	final void testEmptyProductions() {
		TestUtil.assertGenerates("main:;", "");
		TestUtil.assertGenerates("main: ;", "");

		TestUtil.assertGenerates("main:|;", "");
		TestUtil.assertGenerates("main: |;", "");
		TestUtil.assertGenerates("main:| ;", "");

		TestUtil.assertGenerates("main:||;", "");
		TestUtil.assertGenerates("main:| |;", "");
		TestUtil.assertGenerates("main: ||;", "");
		TestUtil.assertGenerates("main:|| ;", "");
	}

	@Test
	public final void testEmptyLeadingProduction() {
		// At least some of the generated strings must be empty

		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main: | bar ;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Grammar<String> grammar = globalContext.getGrammar();
		System.out.println("Grammar:" + grammar);
		boolean generatedEmpty = false;
		for (int i = 1; i <= 100; i++) {
			if (threadContext.generateString().equals("")) {
				generatedEmpty = true;
			}
                }
		Assert.assertTrue(generatedEmpty);
	}

	@Test
	public final void testEmptyTrailingProduction() {
		// At least some of the generated strings must be empty

		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main: bar | ;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		boolean generatedEmpty = false;
		for (int i = 1; i <= 100; i++) {
			if (threadContext.generateString().equals("")) {
				generatedEmpty = true;
			}
                }
		Assert.assertTrue(generatedEmpty);
	}

	@Test
	public final void testTrailingPipe() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("#option TRAILING_PIPES\nmain: foo | bar |\nfoo | bar;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Assert.assertEquals(threadContext.generateString(), "foo | bar");
		Assert.assertEquals(threadContext.generateString(), "foo | bar");
	}

	@Test
	public final void testLeadingColon() {
		TestUtil.assertGenerates("main::;", ":");
		TestUtil.assertGenerates("main: :;", ":");
		TestUtil.assertGenerates("main:: ;", ":");
	}

	@Test
	public final void testMiddleColon() {
		TestUtil.assertGenerates("main: foo: bar;\nfoo:foo2;\nbar:bar2;\n", "foo2: bar2");
		TestUtil.assertGenerates("main: foo :bar;\nfoo:foo2;\nbar:bar2;\n", "foo2 :bar2");
		TestUtil.assertGenerates("main:foo:bar;\nfoo:foo2;\nbar:bar2;\n", "foo2:bar2");
	}

	@Test
	public final void testTrailingColon() {
		TestUtil.assertGenerates("main: foo:;\nfoo:foo2;", "foo2:");
		TestUtil.assertGenerates("main:foo: ;\nfoo:foo2;", "foo2:");
	}

	@Test
	public final void testMiddleSemicolon() {
		TestUtil.assertGenerates("main:foo;bar;", "foo;bar");
		TestUtil.assertGenerates("main:foo; bar;", "foo; bar");
		TestUtil.assertGenerates("main:foo ; bar;", "foo ; bar");
		TestUtil.assertGenerates("main:foo ;bar;", "foo ;bar");
		TestUtil.assertGenerates("main:foo;;bar;", "foo;;bar");
		TestUtil.assertGenerates("main:foo;\nbar:bar2;", "foo");
	}

	@Test
	public final void testStandaloneSemicolon() {
		String grammar = "#option STANDALONE_SEMICOLONS\nmain: foo ; bar ;\n baz;\n;\n";
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Assert.assertEquals(threadContext.generateString(), "foo ; bar ;\n baz;");
	}

	@Test
	public final void testSkipWhitespace() {
		TestUtil.assertGeneratesSkipWhitespace("main: foo bar ;", "foobar");
		TestUtil.assertGeneratesSkipWhitespace("main:  foo  bar  ;", "foobar");
		TestUtil.assertGeneratesSkipWhitespace("main: \\ . / ;", "\\./");

		TestUtil.assertGeneratesSkipWhitespace("main: foo;bar;", "foo;bar");
		TestUtil.assertGeneratesSkipWhitespace("main: foo ; bar;", "foo;bar");
		TestUtil.assertGeneratesSkipWhitespace("main: foo  ;  bar;", "foo;bar");

		TestUtil.assertGeneratesSkipWhitespace("main: foo\nbar;", "foobar");
		TestUtil.assertGeneratesSkipWhitespace("main: foo \n bar;", "foobar");
		TestUtil.assertGeneratesSkipWhitespace("main: foo  \n  bar;", "foobar");

		TestUtil.assertGeneratesSkipWhitespace("main: foo\tbar;", "foobar");
		TestUtil.assertGeneratesSkipWhitespace("main: foo \t bar;", "foobar");
		TestUtil.assertGeneratesSkipWhitespace("main: foo  \t  bar;", "foobar");
	}

	@Test
	public final void testWeightSpecifiers() {
		TestUtil.assertGenerates("main:1 foo ;", "foo");
		TestUtil.assertGenerates("main:100 foo ;", "foo");
		TestUtil.assertGenerates("main:1% foo;", "foo");
		TestUtil.assertGenerates("main:100% foo;", "foo");
		TestUtil.assertGenerates("main:1 foo |1 foo;", "foo");
		TestUtil.assertGenerates("main:1% foo |1% foo;", "foo");
		TestUtil.assertGenerates("main:100 foo |100 foo;", "foo");
		TestUtil.assertGenerates("main:100% foo |100% foo;", "foo");

		TestUtil.assertGenerates("main: 100;", "100");
		TestUtil.assertGenerates("main: 100%;", "100%");

		TestUtil.assertGenerates("main: 100 | 100;", "100");
		TestUtil.assertGenerates("main: 100% | 100%;", "100%");

		TestUtil.assertGenerates("main: 100 foo;", "100 foo");
		TestUtil.assertGenerates("main: 100% foo;", "100% foo");

		TestUtil.assertGenerates("main: 100 foo | 100 foo;", "100 foo");
		TestUtil.assertGenerates("main: 100% foo | 100% foo;", "100% foo");
	}
}
