package org.stoev.fuzzer;

import java.util.List;

interface ResultDigest {
	void addRow(final List<String> row);
	long getDigest();
}
