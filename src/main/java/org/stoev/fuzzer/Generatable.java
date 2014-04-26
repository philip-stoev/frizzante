package org.stoev.fuzzer;

import java.util.Deque;

public interface Generatable {
	void generate(final Context context, final Sentence<?> sentence, final Deque<Generatable> stack);

	void compile(Grammar grammar);
}
