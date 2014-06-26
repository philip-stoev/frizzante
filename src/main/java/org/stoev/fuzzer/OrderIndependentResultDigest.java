package org.stoev.fuzzer;

import java.util.List;

public class OrderIndependentResultDigest implements ResultDigest {
	private long digest = 0;
	static final String NULL_STRING = "<NULL>";
	static final int NULL_STRING_HASHCODE = NULL_STRING.hashCode();

	public final void addRow(final List<String> row) {
		long rowDigest = 0;

		for (int i = 0; i < row.size(); i++) {
			String string = row.get(i);
			int hashCode;

			if (string != null) {
				hashCode = string.hashCode();
			} else {
				hashCode = NULL_STRING_HASHCODE;
			}

			rowDigest = rowDigest + ((i + 1) * hashCode);
		}

		digest = digest ^ rowDigest;
	}

	public final long getDigest() {
		return digest;
	}
}
