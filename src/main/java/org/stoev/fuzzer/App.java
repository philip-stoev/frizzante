package org.stoev.fuzzer;

import org.stoev.fuzzer.Grammar.GrammarFlags;

import java.util.EnumSet;
import java.io.File;

public final class App {
	private App() {	}

	public static void main(final String[] args) throws Throwable {
		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar(new File("mongodb.grammar"), EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
			.idRange(0, 1000L)
			.runnableFactory(new MongoDBRunnableFactory("test4"))
			.count(10000L)
			.threads(10)
			.duration(60)
			.build();

		globalContext.run();
	}
}
