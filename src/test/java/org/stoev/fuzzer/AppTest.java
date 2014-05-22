package org.stoev.fuzzer;

import org.stoev.fuzzer.Grammar.GrammarFlags;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Iterator;
import java.util.EnumSet;

public class AppTest extends TestCase {
	/**
	* Create the test case.
	*
	* @param testName name of the test case
	*/
	public AppTest(final String testName) {
		super(testName);
	}

	/**
	* @return the suite of tests being tested
	*/
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	public final void testParser() {
		String grammar = "main: THIS IS A TEXT | THIS IS SOME OTHER TEXT;";
		Context context = new Context.ContextBuilder().grammar(grammar).build();
		assertEquals(context.generateString(), "THIS IS SOME OTHER TEXT");
	}

	public final void testLinker() {
		String grammar = "main: linker1 , linker2 ;\n linker1: linkerA ;\n linker2: linkerB;";
		Context context = new Context.ContextBuilder().grammar(grammar).build();
		assertEquals("linkerA , linkerB", context.generateString());
	}

	public final void testJavaCode() {
		String grammar = "main: foo ;\n foo.java: {{ sentence.append(\"foo2\"); }};";
		Context context = new Context.ContextBuilder().grammar(grammar).build();
		assertEquals("foo2", context.generateString());
	}

	public final void testForeignGeneratable() {
		String grammar = "main: foo foo;\n foo.java: {{ sentence.add(new Long(2)); }};";
		Context context = new Context.ContextBuilder().grammar(grammar, EnumSet.of(GrammarFlags.SKIP_WHITESPACE)).build();

		Sentence<Long> sentence = new Sentence<Long>();
		context.generate(sentence);
		Iterator<Long> iterator = sentence.iterator();

		Long longValue1 = iterator.next();
		assertEquals(2, longValue1.longValue());

		Long longValue2 = iterator.next();
		assertEquals(2, longValue2.longValue());

		assertFalse(iterator.hasNext());
	}

	public final void testLoops() {
		String grammar = "main: foo | main , foo ;";
		Context context = new Context.ContextBuilder().grammar(grammar).build();
		assertEquals("foo , foo", context.generateString());
	}
}
