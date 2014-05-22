package org.stoev.fuzzer;

// import org.testng.Assert;
import org.testng.annotations.Test;

import org.stoev.fuzzer.Grammar.GrammarFlags;
import java.util.EnumSet;

public class MongoTest {
	@Test
	public final void testMongoJava() {
		Grammar g = new Grammar(Thread.currentThread().getContextClassLoader().getResourceAsStream("mongodb.grammar"), EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY));
		Context c = new Context.ContextBuilder(g).build();
		System.out.println(c.generateString());
		System.out.println(c.generateString());
		System.out.println(c.generateString());
		System.out.println(c.generateString());
		System.out.println(c.generateString());
	}
}
