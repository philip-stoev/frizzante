package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.HashMap;

import org.stoev.fuzzer.GlobalContext.ContextBuilder;

public class SentenceIdRangeTest {
	@Test
	public final void testZeroRange() {
		GlobalContext<String> globalContext = new ContextBuilder<String>().grammar("main: foo foo foo foo foo;\nfoo: foo1 | foo2;").idRange(0, 0).build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);
		String string1 = threadContext.generateString();
		String string2 = threadContext.generateString();

		Assert.assertEquals(string1, string2);
	}

	@Test
	public final void testNarrowRange() {

		Map<String, Integer> map = new HashMap<String, Integer>();

		GlobalContext<String> globalContext = new ContextBuilder<String>().grammar("main: foo foo foo foo foo foo foo foo;\nfoo: foo1 | foo2;").idRange(0, 3).build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		for (int i = 0; i < 1000; i++) {
			String string = threadContext.generateString();
			if (map.get(string) == null) {
				map.put(string, 0);
			} else {
				map.put(string, map.get(string) + 1);
			}
		}

		Assert.assertEquals(map.size(), 4);
	}

	@Test
	public final void testFullRange() {
		Map<String, Integer> map = new HashMap<String, Integer>();

		GlobalContext<String> globalContext = new ContextBuilder<String>().grammar("main: foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo;\nfoo: foo1 | foo2;").build();
		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		for (int i = 0; i < 100; i++) {
			String string = threadContext.generateString();
			if (map.get(string) == null) {
				map.put(string, 0);
			} else {
				map.put(string, map.get(string) + 1);
			}
		}

		Assert.assertEquals(map.size(), 100);
	}

}
