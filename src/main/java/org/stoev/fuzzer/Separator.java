package org.stoev.fuzzer;

class Separator implements Generatable {

	public void generate(final Context context, final Sentence<?> sentence) {
		if (context.getSeparator() != null) {
			sentence.append(context.getSeparator());
		}
	}

	public String toString() {
		return " ";
	}

	public String getName() {
		return " ";
	}

	public void compile(final Grammar grammar) {
		assert false;
	}
}
