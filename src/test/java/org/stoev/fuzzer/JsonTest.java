package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonTest {
	@Test
	public final void testMongo() {
		Grammar g = new Grammar("main: { count:'orders', query: { ord_dt: { $gt: new Date('01/01/2012') } }, skip: 10 };");
                Context c = new Context.ContextBuilder(g).build();
		Assert.assertEquals(c.generateString(), "{ count:'orders', query: { ord_dt: { $gt: new Date('01/01/2012') } }, skip: 10 }");
	}
}
