package org.stoev.fuzzer;

import org.testng.annotations.Test;

public class SeparatorsTest {
	@Test
	public final void testTightSpacing() {
		TestUtil.assertGenerates("main:foo;", "foo");
		TestUtil.assertGenerates("main:foo|foo;", "foo");
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
		TestUtil.assertGenerates("main: foo\tbar;", "foo bar");
		TestUtil.assertGenerates("main: foo\nbar;", "foo bar");
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
		TestUtil.assertGenerates("main:foo|bar;", "bar");
		TestUtil.assertGenerates("main:foo |bar;", "bar");
		TestUtil.assertGenerates("main:foo| bar;", "bar");
		TestUtil.assertGenerates("main:foo|\nbar;", "bar");
		TestUtil.assertGenerates("main:foo|\n\nbar;", "bar");
		TestUtil.assertGenerates("main:foo\n|bar;", "bar");
		TestUtil.assertGenerates("main:foo\n\n|bar;", "bar");
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
	public final void testEmptySeparator() {
		TestUtil.assertGeneratesEmptySeparator("main: foo bar ;", "foobar");
		TestUtil.assertGeneratesEmptySeparator("main:  foo  bar  ;", "foobar");
		TestUtil.assertGeneratesEmptySeparator("main: \\ . / ;", "\\./");

		TestUtil.assertGeneratesEmptySeparator("main: foo;bar;", "foo;bar");
		TestUtil.assertGeneratesEmptySeparator("main: foo ; bar;", "foo;bar");
		TestUtil.assertGeneratesEmptySeparator("main: foo  ;  bar;", "foo;bar");

		TestUtil.assertGeneratesEmptySeparator("main: foo\nbar;", "foobar");
		TestUtil.assertGeneratesEmptySeparator("main: foo \n bar;", "foobar");
		TestUtil.assertGeneratesEmptySeparator("main: foo  \n  bar;", "foobar");

		TestUtil.assertGeneratesEmptySeparator("main: foo\tbar;", "foobar");
		TestUtil.assertGeneratesEmptySeparator("main: foo \t bar;", "foobar");
		TestUtil.assertGeneratesEmptySeparator("main: foo  \t  bar;", "foobar");
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
