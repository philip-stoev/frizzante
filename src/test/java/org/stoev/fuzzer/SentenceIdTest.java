package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.stoev.fuzzer.Context.ContextBuilder;

public class SentenceIdTest {
	@Test
	public final void test() {
		Context<String> context = new ContextBuilder<String>().grammar("main: foo foo foo foo foo;\nfoo: foo1 | foo2;").build();
		Sentence<String> sentence1 = context.newSentence();
		context.generate(sentence1);

		Assert.assertTrue(sentence1.toString().length() > 10);
		Assert.assertTrue(sentence1.getId() != 0);

		Sentence<String> sentence2 = context.sentenceFromId(sentence1.getId());
		context.generate(sentence2);

		Assert.assertEquals(sentence1.toString(), sentence2.toString());
		Assert.assertEquals(sentence1.getId(), sentence2.getId());
	}
}
