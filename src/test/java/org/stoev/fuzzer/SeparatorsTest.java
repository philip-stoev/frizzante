package org.stoev.fuzzer;

import java.util.EnumSet;

import org.stoev.fuzzer.Grammar.GrammarFlags;

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

	@Test (expectedExceptions = ConfigurationException.class)
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
	public final void testTrailingPipe() {
		Context context = new Context.ContextBuilder().grammar("main: foo | bar |\nfoo | bar;", EnumSet.of(GrammarFlags.TRAILING_PIPES_ONLY)).build();

		Assert.assertEquals(context.generateString(), "foo | bar");
		Assert.assertEquals(context.generateString(), "foo | bar");
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
		String grammar = "main: foo ; bar ;\n baz;\n;\n";
		Context context = new Context.ContextBuilder().grammar(grammar, EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY)).build();

		Assert.assertEquals(context.generateString(), "foo ; bar ;\n baz;");
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
