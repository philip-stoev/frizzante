package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class StringMinimizerTest {

	@Test
	public final void testZeroElements() {
		StringMinimizer minimizer = new StringMinimizer("", " ");
		Iterator<String> iterator = minimizer.iterator();
		Assert.assertFalse(iterator.hasNext());
 	}

	@Test
	public final void testOneElement() {
		StringMinimizer minimizer = new StringMinimizer("a", " ");
		Iterator<String> iterator = minimizer.iterator();
		Assert.assertFalse(iterator.hasNext());
 	}

	@Test
	public final void testTwoElementSuccess() {
		StringMinimizer minimizer = new StringMinimizer("a b", " ");
		Iterator<String> iterator = minimizer.iterator();

		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(iterator.next(), "");

		minimizer.succeeded();
		Assert.assertFalse(iterator.hasNext());
 	}

	@Test
	public final void testTwoElementFailure() {
		StringMinimizer minimizer = new StringMinimizer("a b", " ");
		Iterator<String> iterator = minimizer.iterator();

		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(iterator.next(), "");
		minimizer.failed();

		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(iterator.next(), "a ");
		minimizer.failed();

		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(iterator.next(), "b ");
		minimizer.failed();

		Assert.assertFalse(iterator.hasNext());
		Assert.assertEquals(minimizer.getCurrentString(), "a b ");
	}

	@Test
	public final void testMultiElementFailure() {
		StringMinimizer minimizer = new StringMinimizer("1 2 3 4 5 6 7 8", " ");

		for (String s: minimizer) {
			minimizer.failed();
		}

		Assert.assertEquals(minimizer.getCurrentString(), "1 2 3 4 5 6 7 8 ");
	}

	@Test
	public final void testFindOneElement() {
		final String[] strings = {
			"a",

			"a a",
			"a b",
			"b a",

			"a b a",
			"b a b",

			"a a a a a a",
			"a a a b b b",
			"b b b a a a",
			"a b a b a b",
			"b a b a b a",

			"a a a a a a a a",
			"a b a b a b a b",
			"b a b a b a b a",

			"a a a a a a a a a",
			"a a a a a a a a b",
			"b a a a a a a a a",
			"b a b a b a b a b",
			"a b a b a b a b a",


			"a a a a a a a a a a",
			"a a a a a b b b b b",
			"b b b b b a a a a a",
			"a a a a a a a a a b",
			"b a a a a a a a a a"
		};

		for (String originalString: strings) {
			StringMinimizer minimizer = new StringMinimizer(originalString, " ");

			for (String intermediateString: minimizer) {
				if (intermediateString.contains("a")) {
					minimizer.succeeded();
				} else {
					minimizer.failed();
				}
			}

			Assert.assertEquals(minimizer.getCurrentString(), "a ", "Original string:\"" + originalString + "\'");
		}
	}

	@Test
	public final void testFindTwoElements() {
		final String[] strings = {
			"a b",

			"a b a",
			"a b x",
			"x a b",
			"a a b",
			"b a b",

			"a a a b b b",
			"a b a b a b",
			"b a b a b a",

			"a b a b a b a b",
			"b a b a b a b a",

			"a a a a a a a a b",
			"b a b a b a b a b",
			"a b a b a b a b a",


			"a a a a a b b b b b",
			"a a a a a a a a a b",
		};

		for (String originalString: strings) {
			StringMinimizer minimizer = new StringMinimizer(originalString, " ");

			for (String intermediateString: minimizer) {
				if (intermediateString.contains("a b")) {
					minimizer.succeeded();
				} else {
					minimizer.failed();
				}
			}

			Assert.assertEquals(minimizer.getCurrentString(), "a b ", "(1) Original string:\"" + originalString + "\'");
		}

		for (String originalString: strings) {
			StringMinimizer minimizer = new StringMinimizer(originalString, " ");

			for (String intermediateString: minimizer) {
				if (intermediateString.contains("a") && intermediateString.contains("b")) {
					minimizer.succeeded();
				} else {
					minimizer.failed();
				}
			}

			Assert.assertTrue(minimizer.getCurrentString().equals("a b ") || minimizer.getCurrentString().equals("b a "), "(2) Original string:\"" + originalString + "\'");
		}
	}

	@Test
	public final void testRandomMinimizations() {
		Random random = new Random();

		for (int i = 0; i < 1000; i++) {
			final int numGroups = 1 + random.nextInt(5);
			final char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
			final List<String> groups = new ArrayList<String>();

			// Generate random strings

			for (int g = 0; g < numGroups; g++) {
				StringBuilder group = new StringBuilder();
				for (int c = 0; c < random.nextInt(10); c++) {
					group.append(chars[random.nextInt(chars.length)]);
				}
				groups.add(group.toString());
			}

			StringBuilder allGroups = new StringBuilder();
			for (String group: groups) {
				allGroups.append(group);
			}

			// Then disperse them within a string

			StringBuilder fullString = new StringBuilder();

			for (int c = 0; c < random.nextInt(20); c++) {
				fullString.append(chars[random.nextInt(chars.length)]);
			}

			for (String group: groups) {
				fullString.append(group);
				for (int c = 0; c < random.nextInt(20); c++) {
					fullString.append(chars[random.nextInt(chars.length)]);
				}
			}

			// And finally try to minimize them from the larger string

			StringMinimizer minimizer = new StringMinimizer(fullString.toString(), "");

			int minimizationCycles = 0;
			for (String guess: minimizer) {
				minimizationCycles++;
				for (String group: groups) {
					if (!guess.contains(group)) {
						minimizer.failed();
						break;
					}
				}
				minimizer.succeeded();
			}

			Assert.assertTrue(minimizationCycles < (fullString.toString().length() * 2)); // NOPMD

			// The minimized string must never be longer then all the groups taken together

			Assert.assertTrue(minimizer.getCurrentString().length() <= allGroups.toString().length());

			for (String group: groups) {
				Assert.assertTrue(minimizer.getCurrentString().contains(group));
			}
		}
	}
}
