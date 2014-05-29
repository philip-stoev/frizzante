package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

public class SentenceSimplifierTest {

	@Test
	public final void testSimplifyEmptySentence() {
		Context context = new Context.ContextBuilder().grammar("main:;").build();
		Sentence<String> sentence = new Sentence<String>();
		context.generate(sentence);

		SentenceSimplifier simplifier = new SentenceSimplifier(sentence);
		Iterator<Sentence> iterator = simplifier.iterator();
		Assert.assertFalse(iterator.hasNext());
 	}

	@Test
	public final void testSimplifySimpleSentence() {
		Context context = new Context.ContextBuilder().grammar("main: foo bar;").build();
		Sentence<String> sentence = new Sentence<String>();
		context.generate(sentence);

		SentenceSimplifier simplifier = new SentenceSimplifier(sentence);
		Iterator<Sentence> iterator = simplifier.iterator();

		Assert.assertTrue(iterator.hasNext());
		iterator.next();
		simplifier.failed();

		Assert.assertFalse(iterator.hasNext());

		Sentence finalSentence = simplifier.getCurrentSentence();
		Assert.assertEquals(sentence.toString(), finalSentence.toString());
 	}

	@Test
	public final void testSimplifyConstantProduction() {
		// If we signal to the Simplifier that the important characteristics of the generated sentence is the presence of COUNT
		// we expect that the simplifier will choose to retain the FROM DUAL constant production.

		Context context = new Context.ContextBuilder().grammar("main: SELECT COUNT(*) from_clause;\nfrom_clause: FROM DUAL |90% table;\ntable: T1 | T2;").build();

		for (int i = 0; i < 100; i++) {
			Sentence<String> sentence = new Sentence<String>();
			context.generate(sentence);
			SentenceSimplifier simplifier = new SentenceSimplifier(sentence);

			for (Sentence testSentence : simplifier) {
				if (testSentence.toString().contains("COUNT")) {
					simplifier.succeeded();
				} else {
					simplifier.failed();
				}
			}

			Sentence finalSentence = simplifier.getCurrentSentence();
			Assert.assertEquals(finalSentence.toString(), "SELECT COUNT(*) FROM DUAL");
		}
	}

	@Test
	public final void testSimplifyMultipleProductions() {
		Context context = new Context.ContextBuilder().grammar("main: foo bar bar bar bar bar bar;\nbar: bar1 | bar2 | ;").build();

		for (int i = 0; i < 10; i++) {
			Sentence<String> sentence = new Sentence<String>();
			context.generate(sentence);

			SentenceSimplifier simplifier = new SentenceSimplifier(sentence);

			for (Sentence testSentence : simplifier) {
				if (testSentence.toString().contains("bar2")) {
					simplifier.succeeded();
				} else {
					simplifier.failed();
				}
			}

			Sentence finalSentence = simplifier.getCurrentSentence();
			Assert.assertTrue(finalSentence.toString().contains("foo"));
			Assert.assertTrue(finalSentence.toString().contains("bar2"));
			Assert.assertFalse(finalSentence.toString().contains("bar1"));
		}
 	}
}
