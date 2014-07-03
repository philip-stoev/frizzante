package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonTest {
	@Test
	public final void testMongo() {
		String grammar = "main: { count:'orders', query: { ord_dt: { $gt: new Date('01/01/2012') } }, skip: 10 };";
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>().grammar(grammar).build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);
		Assert.assertEquals(threadContext.generateString(), "{ count:'orders', query: { ord_dt: { $gt: new Date('01/01/2012') } }, skip: 10 }");
	}
}
