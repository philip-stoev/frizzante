package org.stoev.fuzzer;

class ProductionInstance<T> {
	private final GrammarProduction<T> production;
	private final int start;
	private int end;

	ProductionInstance(final GrammarProduction<T> p, final int s) {
		assert p != null;

		production = p;
		start = s;
	}

	GrammarProduction<T> getProduction() {
		return production;
	}

	int getStart() {
		return start;
	}

	void setEnd(final int e) {
		// We store the end + 1 in order to be able to distinguish between Productions that produced
		// something and those that did not (where start = end).
		end = e + 1;
		assert end >= start;
	}

	int getEnd() {
		assert end > start;
		return end - 1;
	}

	boolean wasProductive() {
		return end > start;
	}

	public String toString() {
		return production.getParent().getName() + " from " + start + " to " + (end - 1);
	}
}
