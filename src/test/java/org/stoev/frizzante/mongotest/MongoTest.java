package org.stoev.frizzante.test.mongo;

import org.testng.annotations.Test;

import org.stoev.frizzante.GlobalContext;
import org.stoev.frizzante.MongoDBRunnableFactory;
import org.stoev.frizzante.ExecutionException;

import java.io.File;

public class MongoTest {
	@Test (expectedExceptions = ExecutionException.class)
	public final void testMongo() throws Throwable {
                final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
                        .grammar(new File("mongodb.grammar"))
                        .idRange(0, 1000L)
                        .runnableFactory(new MongoDBRunnableFactory("test4"))
                        .count(10000L)
                        .threads(10)
                        .duration(60)
                        .build();

                globalContext.run();
	}
}
