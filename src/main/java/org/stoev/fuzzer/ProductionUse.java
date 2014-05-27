package org.stoev.fuzzer;

class ProductionUse {
	private final GrammarProduction production;
	private final int start;
	private int end;

	ProductionUse(final GrammarProduction p, final int s) {
		production = p;
		start = s;
	}

	GrammarProduction getProduction() {
		return production;
	}

	int getStart() {
		return start;
	}

	void setEnd(final int e) {
		end = e;
	}

	int getEnd() {
		return end;
	}
}
