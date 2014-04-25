package org.stoev.fuzzer;

public interface Generatable {
	void generate(final Context context, final Sentence<?> sentence);

	void compile(Grammar grammar);
}
