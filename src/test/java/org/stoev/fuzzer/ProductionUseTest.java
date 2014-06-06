package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Iterator;

public class ProductionUseTest {
	@Test
	public final void testEmptyGrammar() {
		Context context = new Context.ContextBuilder().grammar("main:;").build();
		Sentence<String> sentence = context.newSentence();
		context.generate(sentence);

		List<ProductionUse> productionUse = sentence.getProductionsUsed();
		Assert.assertTrue(productionUse.isEmpty());
 	}

	@Test
	public final void testEmptyProduction() {
		Context context = new Context.ContextBuilder().grammar("main:foo bar;\nbar:;").build();
		Sentence<String> sentence = context.newSentence();
		context.generate(sentence);

		List<ProductionUse> productionUse = sentence.getProductionsUsed();
		Assert.assertEquals(productionUse.size(), 1);
		Assert.assertEquals(productionUse.get(0).getProduction().getParent().getName(), "main");
 	}


	@Test
	public final void testOneProduction() {
		Context context = new Context.ContextBuilder().grammar("main: foo bar;").build();
		Sentence<String> sentence = context.newSentence();
		context.generate(sentence);
		Assert.assertEquals(sentence.toString(), "foo bar");

		List<ProductionUse> productionsUsed = sentence.getProductionsUsed();
		Assert.assertTrue(!productionsUsed.isEmpty());
		Iterator<ProductionUse> i = productionsUsed.iterator();
		Assert.assertTrue(i.hasNext());
		ProductionUse productionUsed = i.next();
		Assert.assertEquals(productionUsed.getStart(), 0);
		Assert.assertEquals(productionUsed.getEnd(), 2);
		Assert.assertTrue(!i.hasNext());

	}

	@Test
	public final void testTwoProductions() {
		Context context = new Context.ContextBuilder().grammar("main: foo bar;\nfoo: foo2a foo2b;\nbar: bar2a bar2b;\n").build();
		Sentence<String> sentence = context.newSentence();
		context.generate(sentence);
		Assert.assertEquals(sentence.toString(), "foo2a foo2b bar2a bar2b");

		List<ProductionUse> productionsUsed = sentence.getProductionsUsed();
		Assert.assertTrue(!productionsUsed.isEmpty());
		Iterator<ProductionUse> i = productionsUsed.iterator();

		Assert.assertTrue(i.hasNext());
		ProductionUse productionUsed1 = i.next();
		Assert.assertEquals(productionUsed1.getProduction().getParent().getName(), "main");
		Assert.assertEquals(productionUsed1.getStart(), 0);
		Assert.assertEquals(productionUsed1.getEnd(), 6);

		Assert.assertTrue(i.hasNext());
		ProductionUse productionUsed2 = i.next();
		Assert.assertEquals(productionUsed2.getProduction().getParent().getName(), "foo");
		Assert.assertEquals(productionUsed2.getStart(), 0);
		Assert.assertEquals(productionUsed2.getEnd(), 2);

		Assert.assertTrue(i.hasNext());
		ProductionUse productionUsed3 = i.next();
		Assert.assertEquals(productionUsed3.getProduction().getParent().getName(), "bar");
		Assert.assertEquals(productionUsed3.getStart(), 4);
		Assert.assertEquals(productionUsed3.getEnd(), 6);

		Assert.assertTrue(!i.hasNext());
	}

}
