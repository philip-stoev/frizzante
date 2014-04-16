package org.stoev.fuzzer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Random;
import java.util.List;
import java.util.LinkedList;

/**
 * Unit test for simple App.
 */
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
		Context context = new Context(new Random(1), "<separator>");
		StringBuilder buffer = new StringBuilder();
		grammar.generate(context, buffer);
		assertEquals(buffer.toString(), "THIS<separator>IS<separator>SOME<separator>OTHER<separator>TEXT");
	}

	public final void testLinker() {
		Grammar grammar = new Grammar("main: foo , bar ; foo: foo2 ; bar: bar2;");
		Context context = new Context(new Random(1), " ");
		StringBuilder buffer = new StringBuilder();
		grammar.generate(context, buffer);
		assertEquals(buffer.toString(), "foo2 , bar2");
	}

	public final void testJavaCode() {
		Grammar grammar = new Grammar("main: foo ; foo.java: { buffer.append(\"foo2\"); };");
		Context context = new Context(new Random(1), " ");
		StringBuilder buffer = new StringBuilder();
		grammar.generate(context, buffer);
		assertEquals("foo2", buffer.toString());
	}

	public final void testCaching() {
		Grammar grammar = new Grammar("main: foo , $foo ; foo: foo2 ;");
		Context context = new Context(grammar, new Random(1), " ");
		StringBuilder buffer = new StringBuilder();
		grammar.generate(context, buffer);
		assertEquals(buffer.toString(), "foo2 , foo2");
	}

	public final void testLoops() {
		Grammar grammar = new Grammar("main: foo | main , foo ;");
		Context context = new Context(new Random(1), " ");
		StringBuilder buffer = new StringBuilder();
		grammar.generate(context, buffer);
		assertEquals(buffer.toString(), "foo , foo");
	}

	public final void testLiteral() {
		Context context = new Context(new Random(1), "<separator>");
		StringBuilder buffer = new StringBuilder();
		(new Literal("ABC")).generate(context, buffer);
		assertEquals(buffer.toString(), "ABC");
	}

	public final void testEmptyLiteral() {
		Context context = new Context(new Random(1), "<separator>");
		StringBuilder buffer = new StringBuilder();
		(new Literal("")).generate(context, buffer);
		assertEquals(buffer.toString(), "");
	}

	public final void testEmptySeparator() {
		List<Generatable> elements = new LinkedList<Generatable>();
		elements.add(new Literal("ABC"));
		elements.add(new Literal("XYZ"));

		Context context = new Context(new Random(1), "");
		GrammarProduction production = new GrammarProduction(elements);

		StringBuilder buffer = new StringBuilder();
		production.generate(context, buffer);
		assertEquals(buffer.toString(), "ABCXYZ");
	}

	public final void testGrammarProductions() {
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

	public final void testGrammarRule() {
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
}
