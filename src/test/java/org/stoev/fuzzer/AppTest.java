package org.stoev.fuzzer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Iterator;

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
		Grammar grammar = new Grammar("main: THIS IS A TEXT | THIS IS SOME OTHER TEXT;");
		Context context = new Context.ContextBuilder(grammar).separator("<separator>").build();
		assertEquals(context.generateString(), "THIS<separator>IS<separator>SOME<separator>OTHER<separator>TEXT");
	}

	public final void testLinker() {
		Grammar grammar = new Grammar("main: linker1 , linker2 ; linker1: linkerA ; linker2: linkerB;");
		Context context = new Context.ContextBuilder(grammar).build();
		assertEquals("linkerA , linkerB", context.generateString());
	}

	public final void testJavaCode() {
		Grammar grammar = new Grammar("main: foo ; foo.java: {{ sentence.add(\"foo2\"); }};");
		Context context = new Context.ContextBuilder(grammar).build();
		assertEquals("foo2", context.generateString());
	}

	public final void testForeignGeneratable() {
		Grammar grammar = new Grammar("main: foo foo; foo.java: {{ sentence.add(new Long(2)); }};");
		Context context = new Context.ContextBuilder(grammar).build();

		Sentence<Long> sentence = new Sentence<Long>();
		context.generate(sentence);
		Iterator<Long> iterator = sentence.iterator();

		Long longValue1 = iterator.next();
		assertEquals(2, longValue1.longValue());

		Long longValue2 = iterator.next();
		assertEquals(2, longValue2.longValue());

		assertFalse(iterator.hasNext());
	}

	public final void testCaching() {
		Grammar grammar = new Grammar("main: foo , $foo ; foo: foo2 ;");
		Context context = new Context.ContextBuilder(grammar).build();
		assertEquals(context.generateString(), "foo2 , foo2");
	}

	public final void testLoops() {
		Grammar grammar = new Grammar("main: foo | main , foo ;");
		Context context = new Context.ContextBuilder(grammar).build();
		assertEquals("foo , foo", context.generateString());
	}
}
