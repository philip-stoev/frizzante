package org.stoev.fuzzer;

import org.testng.annotations.Test;

public class CommentTest {
	@Test
	public final void testCommentedRule() {
		TestUtil.assertGenerates("main: foo bar;\n#foo: foo1;\nfoo: foo2;", "foo2 bar");
		TestUtil.assertGenerates("main: foo bar;\nfoo: foo1;\n#foo: foo2;", "foo1 bar");
	}

	@Test
	public final void testLeadingComment() {
		TestUtil.assertGenerates("#This is a comment\nmain: foo bar;", "foo bar");
	}

	@Test
	public final void testMiddleComment() {
		TestUtil.assertGenerates("main: foo # bar;\n", "foo # bar");
		TestUtil.assertGenerates("main: foo \n#comment\n bar;\n", "foo \n#comment\n bar");
	}

	@Test
	public final void testTrailingComment() {
		TestUtil.assertGenerates("main: foo bar;\n#This is a comment\n", "foo bar");
	}

}
