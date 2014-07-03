package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.EnumSet;

import org.stoev.fuzzer.Grammar.GrammarFlags;

public class CachingTest {

	@Test
	public final void testCachingSentence() {
		String grammar = "main: foo , foo_cached ;\n foo: foo2 ;";
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Assert.assertEquals(threadContext.generateString(), "foo2 , foo2");
		Assert.assertEquals(threadContext.generateString(), "foo2 , foo2");
	}

	@Test
	public final void testCachingObject() {
		String grammar = "main: bar bar_cached;\n bar.java: {{ sentence.add(new Long(sentence.randomInt(100))); }};";
		GlobalContext<Long> globalContext = new GlobalContext.ContextBuilder<Long>().grammar(grammar, EnumSet.of(GrammarFlags.SKIP_WHITESPACE)).build();
		ThreadContext<Long> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Sentence<Long> sentence = threadContext.newSentence();
		threadContext.generate(sentence);

		Iterator<Long> iterator = sentence.iterator();
		Long longValue1 = iterator.next();
		Long longValue2 = iterator.next();

		Assert.assertEquals(longValue1.longValue(), longValue2.longValue());
	}
}
