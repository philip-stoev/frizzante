package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.HashMap;

import org.stoev.fuzzer.Context.ContextBuilder;

public class SentenceIdRangeTest {
	@Test
	public final void testZeroRange() {
		Context<String> context = new ContextBuilder<String>().grammar("main: foo foo foo foo foo;\nfoo: foo1 | foo2;").idRange(0, 0).build();
		String string1 = context.generateString();
		String string2 = context.generateString();

		Assert.assertEquals(string1, string2);
	}

	@Test
	public final void testNarrowRange() {

		Map<String, Integer> map = new HashMap<String, Integer>();

		Context<String> context = new ContextBuilder<String>().grammar("main: foo foo foo foo foo foo foo foo;\nfoo: foo1 | foo2;").idRange(0, 3).build();

		for (int i = 0; i < 1000; i++) {
			String string = context.generateString();
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

		Context<String> context = new ContextBuilder<String>().grammar("main: foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo foo;\nfoo: foo1 | foo2;").build();

		for (int i = 0; i < 100; i++) {
			String string = context.generateString();
			if (map.get(string) == null) {
				map.put(string, 0);
			} else {
				map.put(string, map.get(string) + 1);
			}
		}

		Assert.assertEquals(map.size(), 100);
	}

}
