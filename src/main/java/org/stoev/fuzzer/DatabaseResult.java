package org.stoev.fuzzer;

import java.util.List;

public final class DatabaseResult {
	private int errorCode;
	private final ResultDigest resultDigest = new OrderIndependentResultDigest();
	private int resultSize = 0;
	
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public void addRow(List<String> row) {
		resultDigest.addRow(row);
		resultSize++;
	}

	public ResultComparison compareTo(DatabaseResult otherResult) {
		if (this.errorCode != otherResult.errorCode) {
			return ResultComparison.COMPARISON_ERROR_MISMATCH;
		}

		if (this.resultSize != otherResult.resultSize) {
			return ResultComparison.COMPARISON_LENGTH_MISMATCH;
		}

		if (this.resultDigest.getDigest() != otherResult.resultDigest.getDigest()) {
			return ResultComparison.COMPARISON_CONTENT_MISMATCH;
		}

		return ResultComparison.COMPARISON_IDENTICAL;
	}
}
