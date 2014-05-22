package org.stoev.fuzzer;

final class Separator implements Generatable {
	private static final Separator SEPARATOR = new Separator();

	private Separator() { };

	public static Separator getSeparator() {
		return SEPARATOR;
	}

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
