package org.stoev.fuzzer;

// import org.testng.Assert;
import org.testng.annotations.Test;

import org.stoev.fuzzer.Grammar.GrammarFlags;
import java.util.EnumSet;

public class MongoTest {
	@Test
	public final void testMongoJava() {
		Context c = new Context.ContextBuilder()
			.grammar(Thread.currentThread().getContextClassLoader().getResourceAsStream("mongodb.grammar"), EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
			.build();
		System.out.println(c.generateString());
		System.out.println(c.generateString());
		System.out.println(c.generateString());
		System.out.println(c.generateString());
		System.out.println(c.generateString());
	}
}
