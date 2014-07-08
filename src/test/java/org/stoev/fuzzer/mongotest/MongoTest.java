package org.stoev.fuzzer.mongotest;

// import org.testng.Assert;
// import org.testng.annotations.Test;

import org.stoev.fuzzer.Grammar.GrammarFlags;
import org.stoev.fuzzer.GlobalContext;
import org.stoev.fuzzer.MongoDBRunnable;

import java.util.EnumSet;
import java.io.File;

public class MongoTest {
//	@Test
	public final void testMongo() throws Throwable {
                final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
                        .grammar(new File("mongodb.grammar"), EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
                        .idRange(0, 1000L)
                        .runnable(MongoDBRunnable.class)
                        .count(10000L)
                        .threads(10)
                        .duration(60)
                        .build();

                globalContext.run();
	}
}
