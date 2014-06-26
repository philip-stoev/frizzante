package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.ArrayList;

public class OrderIndependentResultDigestTest {
	@Test
	public final void testEmptyDigest() {
		ResultDigest digest = new OrderIndependentResultDigest();
		Assert.assertEquals(digest.getDigest(), 0);
	}

	@Test
	public final void testNullElement() {
		ResultDigest digest = new OrderIndependentResultDigest();

		List<String> row = new ArrayList<String>();
		row.add(null);

		long digest1 = digest.getDigest();
		digest.addRow(row);
		long digest2 = digest.getDigest();

		Assert.assertNotEquals(digest1, digest2);
		Assert.assertNotEquals(digest2, 0);
	}

	@Test
	public final void testElementOrder() {
		ResultDigest digest1 = new OrderIndependentResultDigest();
		List<String> row1 = new ArrayList<String>();
		row1.add("foo");
		row1.add("");
		digest1.addRow(row1);

		ResultDigest digest2 = new OrderIndependentResultDigest();
		List<String> row2 = new ArrayList<String>();
		row2.add("");
		row2.add("foo");
		digest2.addRow(row2);

		Assert.assertNotEquals(digest1.getDigest(), digest2.getDigest());
		Assert.assertNotEquals(digest1.getDigest(), 0);
	}

	@Test
	public final void testGroupOrder() {

		List<String> row1 = new ArrayList<String>();
		row1.add("foo");
		List<String> row2 = new ArrayList<String>();
		row1.add("bar");
		List<String> row3 = new ArrayList<String>();
		row1.add("baz");

		ResultDigest digest1 = new OrderIndependentResultDigest();
		digest1.addRow(row1);
		digest1.addRow(row2);
		digest1.addRow(row3);

		ResultDigest digest2 = new OrderIndependentResultDigest();
		digest2.addRow(row3);
		digest2.addRow(row2);
		digest2.addRow(row1);

		Assert.assertEquals(digest1.getDigest(), digest2.getDigest());
		Assert.assertNotEquals(digest1.getDigest(), 0);
	}
}
