package org.stoev.fuzzer;

class CachedValue<T> implements Generatable<T> {
	private final String ruleName;

	CachedValue(final String rn) {
		ruleName = rn;
	}

	public void generate(final ThreadContext<T> threadContext, final Sentence<T> sentence) {
		sentence.addAll(threadContext.getCachedValue(ruleName));
	}

	public String toString() {
		return ruleName + "_cached";
	}

	public String getName() {
		return ruleName + "_cached";
	}

	public void compile(final Grammar<T> grammar) {
		assert false;
	}

	public boolean isConstant() {
		// TODO - determine isConstant() based on the value being cached
		return false;
	}
}
