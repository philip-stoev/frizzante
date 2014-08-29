package org.stoev.frizzante;

import org.testng.annotations.Test;

public class IncludeTest {
	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testMalformedInclude() {
                GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("#include\n").build();
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testIncludeNonexistentFile() {
                GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("#include no_such_file\n").build();
	}

	@Test
	public final void testSimpleInclude() {
		TestUtil.assertGenerates("#include foo.include\nmain: foo;", "foo2");
	}

	@Test
	public final void testConsequtiveIncludes() {
		TestUtil.assertGenerates("#include foo.include\n#include bar.include\nmain: foo bar;", "foo2 bar2");
	}

}
