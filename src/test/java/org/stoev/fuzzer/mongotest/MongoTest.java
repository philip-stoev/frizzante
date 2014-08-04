package org.stoev.fuzzer.test.mongo;

import org.testng.annotations.Test;

import org.stoev.fuzzer.GlobalContext;
import org.stoev.fuzzer.MongoDBRunnableFactory;
import org.stoev.fuzzer.ExecutionException;

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
