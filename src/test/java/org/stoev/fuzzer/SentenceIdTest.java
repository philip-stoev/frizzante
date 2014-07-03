package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.stoev.fuzzer.GlobalContext.ContextBuilder;

public class SentenceIdTest {
	@Test
	public final void test() {
		GlobalContext<String> globalContext = new ContextBuilder<String>().grammar("main: foo foo foo foo foo;\nfoo: foo1 | foo2;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);
		
		Sentence<String> sentence1 = threadContext.newSentence();
		threadContext.generate(sentence1);

		Assert.assertTrue(sentence1.toString().length() > 10);
		Assert.assertTrue(sentence1.getId() != 0);

		Sentence<String> sentence2 = globalContext.sentenceFromId(sentence1.getId());
		threadContext.generate(sentence2);

		Assert.assertEquals(sentence1.toString(), sentence2.toString());
		Assert.assertEquals(sentence1.getId(), sentence2.getId());
	}
}
