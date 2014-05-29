package org.stoev.fuzzer;

import org.testng.annotations.Test;

public class ShortestConstantSentenceTest {

	@Test
	public final void testEmptyShortestConstantSentence() {
		TestUtil.assertShortestConstantSentence("main:;", "");
 	}

	@Test
	public final void testSingleShortestConstantSentence() {
		TestUtil.assertShortestConstantSentence("main: foo;", "foo");
 	}

	@Test
	public final void testTwoShortestConstantSentence() {
		TestUtil.assertShortestConstantSentence("main: foo foo | bar ;", "bar");
		TestUtil.assertShortestConstantSentence("main: bar | foo foo ;", "bar");
 	}

	@Test
	public final void testNestedConstantSentence() {
		TestUtil.assertShortestConstantSentence("main: foo ;\n foo: bar bar;", "bar bar");
 	}

	@Test
	public final void testTreeConstantSentence() {
		TestUtil.assertShortestConstantSentence("main: foo | bar | baz ;\n foo: foo2 foo2;\n bar: bar2;\nbaz: baz1 | baz2;", "bar2");
 	}
}
