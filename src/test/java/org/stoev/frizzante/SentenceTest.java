package org.stoev.frizzante;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Formatter;

public class SentenceTest {
	@Test (expectedExceptions = ClassCastException.class)
	public final void testIncompatibleClasses() {
		Sentence<Boolean> s = Sentence.newSentence(1);
		s.append("abc");
		for (Boolean b: s) {
			Assert.fail("Iterator should have failed.");
		}
	};

	@Test
	public final void testAppendable() {
		Sentence<String> s = Sentence.newSentence(1);
		Formatter formatter = new Formatter(s);
		formatter.format("%s", "abc");
		Assert.assertEquals(s.toString(), "abc");
	}

	@Test
	public final void testObjectToString() {
		Sentence<Long> s = Sentence.newSentence(1);
		s.add(1L);
		s.add(2L);
		Assert.assertEquals(s.toString(), "12");
	}

	@Test
	public final void testIterator() {
		Sentence<Boolean> s = Sentence.newSentence(1);
		s.add(Boolean.TRUE);
		s.add(Boolean.FALSE);
		Iterator<Boolean> i = s.iterator();

		Assert.assertTrue(i.next());
		Assert.assertFalse(i.next());
	}

	@Test
	public final void testNullElements() {
		Sentence<String> s = Sentence.newSentence(1);
		s.add(null);
		s.append(null);
		Iterator<String> i = s.iterator();

		Assert.assertNull(i.next());
		Assert.assertNull(i.next());

		Assert.assertEquals(s.toString(), "nullnull");
	}
}
