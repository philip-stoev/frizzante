package org.stoev.frizzante;

import org.testng.annotations.Test;

public class StringifyTest {
	@Test
	public final void testEmpty() {
		TestUtil.assertToString("main:;", "main:1.0 \n;\n");
		TestUtil.assertToString("main:;\n", "main:1.0 \n;\n");
		TestUtil.assertToString("main: ;", "main:1.0 \n;\n");
		TestUtil.assertToString("main:  ;", "main:1.0 \n;\n");
	}

	@Test
	public final void testEmptyProductions() {
		TestUtil.assertToString("main:| |  |   ;", "main:1.0 \n|1.0 \n|1.0 \n|1.0 \n;\n");
	}

	@Test
	public final void testProductions() {
		TestUtil.assertToString("main:foo|bar;", "main:1.0 foo\n|1.0 bar\n;\n");
		TestUtil.assertToString("main:foo bar baz;", "main:1.0 foo bar baz\n;\n");
		TestUtil.assertToString("main:foo ; bar ;", "main:1.0 foo ; bar\n;\n");
	}

	@Test
	public final void testWeights() {
		TestUtil.assertToString("main:2 foo |3 bar;", "main:2.0 foo\n|3.0 bar\n;\n");
	}

	@Test
	public final void testRules() {
		TestUtil.assertToString("main: foo | bar;\nbar: baz;", "bar:1.0 baz\n;\nmain:1.0 foo\n|1.0 bar\n;\n");

	}

	@Test
	public final void testInlineJava() {
		TestUtil.assertToString("main.java: {{ int i; }};", "main:{{ int i; }};\n");
	}

	@Test
	public final void testCached() {
		TestUtil.assertToString("main:foo_cached;", "main:1.0 foo_cached\n;\n");
	}

}
