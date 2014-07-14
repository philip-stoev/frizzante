package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Iterator;

public class ProductionInstanceTest {
	@Test
	public final void testEmptyGrammar() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main:;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);
		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);

		List<ProductionInstance<String>> productionInstances = sentence.getProductionInstances();
		Assert.assertEquals(productionInstances.size(), 1);
		Assert.assertEquals(productionInstances.get(0).getProduction().getParent().getName(), "main");
 	}

	@Test
	public final void testEmptyProduction() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main:foo bar;\nbar:;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);

		List<ProductionInstance<String>> productionInstances = sentence.getProductionInstances();
		Assert.assertEquals(productionInstances.size(), 2);
		Assert.assertEquals(productionInstances.get(0).getProduction().getParent().getName(), "main");
		Assert.assertEquals(productionInstances.get(1).getProduction().getParent().getName(), "bar");
 	}


	@Test
	public final void testOneProduction() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main: foo bar;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);
		Assert.assertEquals(sentence.toString(), "foo bar");

		List<ProductionInstance<String>> productionInstances = sentence.getProductionInstances();
		Assert.assertTrue(!productionInstances.isEmpty());
		Iterator<ProductionInstance<String>> i = productionInstances.iterator();
		Assert.assertTrue(i.hasNext());
		ProductionInstance<String> productionInstance = i.next();
		Assert.assertEquals(productionInstance.getStart(), 0);
		Assert.assertEquals(productionInstance.getEnd(), 2);
		Assert.assertTrue(!i.hasNext());

	}

	@Test
	public final void testTwoProductions() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar("main: foo bar;\nfoo: foo2a foo2b;\nbar: bar2a bar2b;\n").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);
		Assert.assertEquals(sentence.toString(), "foo2a foo2b bar2a bar2b");

		List<ProductionInstance<String>> productionInstances = sentence.getProductionInstances();
		Assert.assertTrue(!productionInstances.isEmpty());
		Iterator<ProductionInstance<String>> i = productionInstances.iterator();

		Assert.assertTrue(i.hasNext());
		ProductionInstance<String> productionInstance1 = i.next();
		Assert.assertEquals(productionInstance1.getProduction().getParent().getName(), "main");
		Assert.assertEquals(productionInstance1.getStart(), 0);
		Assert.assertEquals(productionInstance1.getEnd(), 6);

		Assert.assertTrue(i.hasNext());
		ProductionInstance<String> productionInstance2 = i.next();
		Assert.assertEquals(productionInstance2.getProduction().getParent().getName(), "foo");
		Assert.assertEquals(productionInstance2.getStart(), 0);
		Assert.assertEquals(productionInstance2.getEnd(), 2);

		Assert.assertTrue(i.hasNext());
		ProductionInstance<String> productionInstance3 = i.next();
		Assert.assertEquals(productionInstance3.getProduction().getParent().getName(), "bar");
		Assert.assertEquals(productionInstance3.getStart(), 4);
		Assert.assertEquals(productionInstance3.getEnd(), 6);

		Assert.assertTrue(!i.hasNext());
	}
}
