package org.stoev.frizzante;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

public class SentenceSimplifierTest {

	@Test
	public final void testSimplifyEmptySentence() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main:;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);
		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);

		SentenceSimplifier<String> simplifier = new SentenceSimplifier<String>(sentence);
		Iterator<Sentence<String>> iterator = simplifier.iterator();
		Assert.assertFalse(iterator.hasNext());
 	}

	@Test
	public final void testSimplifyNoAlternatives() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main: foo bar;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);

		SentenceSimplifier<String> simplifier = new SentenceSimplifier<String>(sentence);
		Iterator<Sentence<String>> iterator = simplifier.iterator();
		Assert.assertFalse(iterator.hasNext());
 	}

	@Test
	public final void testSimplifyEmptyProduction() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main: foo bar | ;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		for (int i = 0; i < 10; i++) {
			Sentence<String> sentence = threadContext.newSentence();
			threadContext.generate(sentence);

			if (sentence.toString().equals("")) {
				continue;
			}

			SentenceSimplifier<String> simplifier = new SentenceSimplifier<String>(sentence);
			Iterator<Sentence<String>> iterator = simplifier.iterator();

			Assert.assertTrue(iterator.hasNext());
			Sentence<String> testSentence = iterator.next();
			Assert.assertFalse(iterator.hasNext());
			Assert.assertEquals(testSentence.toString(), "");
		}
	}

	@Test
	public final void testSimplifyConstantProduction() {
		// If we signal to the Simplifier that the important characteristics of the generated Sentence<String> is the presence of COUNT
		// we expect that the simplifier will choose to retain the FROM DUAL constant production.

		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main: SELECT COUNT(*) from_clause;\nfrom_clause: FROM DUAL |90% table;\ntable: T1 | T2;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		for (int i = 0; i < 100; i++) {
			Sentence<String> sentence = threadContext.newSentence();
			threadContext.generate(sentence);
			SentenceSimplifier<String> simplifier = new SentenceSimplifier<String>(sentence);

			for (Sentence<String> testSentence : simplifier) {
				if (testSentence.toString().contains("COUNT")) {
					simplifier.succeeded();
				} else {
					simplifier.failed();
				}
			}

			Sentence<String> finalSentence = simplifier.getCurrentSentence();
			Assert.assertEquals(finalSentence.toString(), "SELECT COUNT(*) FROM DUAL");
		}
	}

	@Test
	public final void testSimplifyMultipleProductions() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main: foo bar bar bar bar bar bar ;\nbar: bar1 | bar2 | ;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		for (int i = 0; i < 100; i++) {
			Sentence<String> sentence = threadContext.newSentence();
			threadContext.generate(sentence);

			// For the purposes of this test, only initial sentences that contain bar2 are good.

			if (!sentence.toString().contains("bar2")) {
				continue;
			}

			SentenceSimplifier<String> simplifier = new SentenceSimplifier<String>(sentence);

			for (Sentence<String> testSentence : simplifier) {
				if (testSentence.toString().contains("bar2")) {
					simplifier.succeeded();
				} else {
					simplifier.failed();
				}
			}

			Sentence<String> finalSentence = simplifier.getCurrentSentence();
			Assert.assertTrue(finalSentence.toString().contains("foo"));
			Assert.assertTrue(finalSentence.toString().contains("bar2"));
			Assert.assertFalse(finalSentence.toString().contains("bar1"));
		}
 	}
}
