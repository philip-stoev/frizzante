package org.stoev.fuzzer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
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

	public final void testParser() throws IOException {
		Grammar grammar = new Grammar("main: THIS IS A TEXT | THIS IS SOME OTHER TEXT;");
		Context context = new Context(grammar, "<separator>");
		assertEquals(context.generateString(), "THIS<separator>IS<separator>SOME<separator>OTHER<separator>TEXT");
	}

	public final void testLinker() throws IOException {
		Grammar grammar = new Grammar("main: linker1 , linker2 ; linker1: linkerA ; linker2: linkerB;");
		Context context = new Context(grammar, " ");
		assertEquals(context.generateString(), "linkerA , linkerB");
	}

	public final void testJavaCode() throws IOException {
		Grammar grammar = new Grammar("main: foo ; foo.java: { sentence.add(\"foo2\"); };");
		Context context = new Context(grammar, " ");
		assertEquals("foo2", context.generateString());
	}

	public final void testForeignGeneratable() throws IOException {
		Grammar grammar = new Grammar("main: foo foo; foo.java: { sentence.add(new Long(2)); };");
		Context context = new Context(grammar);

		Sentence<Long> sentence = new Sentence<Long>();
		grammar.generate(context, sentence);
		Iterator<Long> iterator = sentence.iterator();

		Long longValue1 = iterator.next();
		assertEquals(2, longValue1.longValue());

		Long longValue2 = iterator.next();
		assertEquals(2, longValue2.longValue());

		assertFalse(iterator.hasNext());
	}

	public final void testGeneratableString() throws IOException {
		Grammar grammar = new Grammar("main: ABC XYZ;");
		Context context = new Context(grammar);
		Sentence<String> sentence = new Sentence<String>(" ");
		grammar.generate(context, sentence);
		assertEquals("ABC XYZ", sentence.toString());
	}

/*
	public final void testCaching() throws IOException {
		Grammar grammar = new Grammar("main: foo , $foo ; foo: foo2 ;");
		Context context = new Context(grammar, new Random(1), " ");
		StringBuilder buffer = new StringBuilder();
		grammar.generate(context, buffer);
		assertEquals(buffer.toString(), "foo2 , foo2");
	}

	public final void testLoops() throws IOException {
		Grammar grammar = new Grammar("main: foo | main , foo ;");
		Context context = new Context(new Random(1), " ");
		StringBuilder buffer = new StringBuilder();
		grammar.generate(context, buffer);
		assertEquals(buffer.toString(), "foo , foo");
	}

	public final void testLiteral() throws IOException {
		Context context = new Context(new Random(1), "<separator>");
		StringBuilder buffer = new StringBuilder();
		(new Literal("ABC")).generate(context, buffer);
		assertEquals(buffer.toString(), "ABC");
	}

	public final void testEmptyLiteral() throws IOException {
		Context context = new Context(new Random(1), "<separator>");
		StringBuilder buffer = new StringBuilder();
		(new Literal("")).generate(context, buffer);
		assertEquals(buffer.toString(), "");
	}

	public final void testEmptySeparator() throws IOException {
		List<Generatable> elements = new LinkedList<Generatable>();
		elements.add(new Literal("ABC"));
		elements.add(new Literal("XYZ"));

		Context context = new Context(new Random(1), "");
		GrammarProduction production = new GrammarProduction(elements);

		StringBuilder buffer = new StringBuilder();
		production.generate(context, buffer);
		assertEquals(buffer.toString(), "ABCXYZ");
	}

	public final void testGrammarProductions() throws IOException {
		List<Generatable> elements = new LinkedList<Generatable>();

		Context emptyContext = new Context(new Random(1), "<separator>");
		GrammarProduction emptyProduction = new GrammarProduction(elements);
		StringBuilder emptyBuffer = new StringBuilder();
		emptyProduction.generate(emptyContext, emptyBuffer);
		assertEquals(emptyBuffer.toString(), "");

		elements.add(new Literal("ABC"));
		Context oneItemContext = new Context(new Random(1), " ");
		GrammarProduction oneItemProduction = new GrammarProduction(elements);
		StringBuilder oneItemBuffer = new StringBuilder();
		oneItemProduction.generate(oneItemContext, oneItemBuffer);
		assertEquals(oneItemBuffer.toString(), "ABC");

		elements.add(new Literal("KLM"));
		Context twoItemContext = new Context(new Random(1), " ");
		GrammarProduction twoItemProduction = new GrammarProduction(elements);
		StringBuilder twoItemBuffer = new StringBuilder();
		twoItemProduction.generate(twoItemContext, twoItemBuffer);
		assertEquals(twoItemBuffer.toString(), "ABC KLM");

		elements.add(new Literal("XYZ"));
		Context threeItemContext = new Context(new Random(1), " ");
		GrammarProduction threeItemProduction = new GrammarProduction(elements);
		StringBuilder threeItemBuffer = new StringBuilder();
		threeItemProduction.generate(threeItemContext, threeItemBuffer);
		assertEquals(threeItemBuffer.toString(), "ABC KLM XYZ");
	}

	public final void testGrammarRule() throws IOException {
		List<Generatable> innerProductions = new LinkedList<Generatable>();
		List<Generatable> innerElements = new LinkedList<Generatable>();
		innerElements.add(new Literal("XYZ"));
		GrammarProduction innerProduction = new GrammarProduction(innerElements);
		innerProductions.add(innerProduction);
		GrammarRule innerRule = new GrammarRule("foo", innerProductions);

		List<Generatable> outerProductions = new LinkedList<Generatable>();
		List<Generatable> outerElements = new LinkedList<Generatable>();

		outerElements.add(new Literal("ABC"));
		outerElements.add(innerRule);
		Generatable outerProduction = new GrammarProduction(outerElements);
		outerProductions.add(outerProduction);
		GrammarRule outerRule = new GrammarRule("bar", outerProductions);

		Context context = new Context(new Random(1), " ");
		StringBuilder buffer = new StringBuilder();
		outerRule.generate(context, buffer);

		assertEquals(buffer.toString(), "ABC XYZ");
	}

*/
}
