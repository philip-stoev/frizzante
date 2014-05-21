package org.stoev.fuzzer;

// import org.testng.Assert;
import org.testng.annotations.Test;

public class MongoTest {
	@Test
	public final void testMongoJava() {
		Grammar g = new Grammar(Thread.currentThread().getContextClassLoader().getResourceAsStream("mongodb.grammar"));
		Context c = new Context.ContextBuilder(g).separator("").build();
		System.out.println(c.generateString());
		System.out.println(c.generateString());
		System.out.println(c.generateString());
		System.out.println(c.generateString());
		System.out.println(c.generateString());
	}
}
