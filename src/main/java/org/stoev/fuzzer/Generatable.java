package org.stoev.fuzzer;
import java.io.IOException;

public interface Generatable {
	void generate(final Context context, final Sentence<?> sentence) throws IOException;

	void compile(Grammar grammar);
}
