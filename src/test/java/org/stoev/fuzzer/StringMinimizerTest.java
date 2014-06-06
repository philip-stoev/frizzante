package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

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

			Assert.assertEquals(minimizer.getCurrentString(), "a b ", "Original string:\"" + originalString + "\'");
		}
	}

}
