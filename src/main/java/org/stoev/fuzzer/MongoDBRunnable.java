package org.stoev.fuzzer;

import com.mongodb.MongoClient;
import com.mongodb.DB;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public final class MongoDBRunnable extends JavaBatchRunnable {

	protected String[] getImports() {
		return new String[] {
			"import org.stoev.fuzzer.ThreadContext",
			"import com.mongodb.DB",
			"import com.mongodb.CommandFailureException",
			"import com.mongodb.WriteConcernException",
			"import com.mongodb.DBCollection",
			"import com.mongodb.DBCursor",
			"import com.mongodb.BasicDBObject",
			"import com.mongodb.BulkWriteOperation"
		};
	};

	private final MongoClient mongoClient;
	private final DB db;

	public MongoDBRunnable(final RunnableManager runnableManager, final ThreadContext<String> threadContext) {
		super(runnableManager, threadContext);

		try {
			mongoClient = new MongoClient();
			db = mongoClient.getDB("test2");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void invoke(final Method method) throws IllegalAccessException, InvocationTargetException {
		method.invoke(null, threadContext, db);
	}
}
