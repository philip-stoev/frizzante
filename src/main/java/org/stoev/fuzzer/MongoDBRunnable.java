package org.stoev.fuzzer;

import com.mongodb.MongoClient;
import com.mongodb.DB;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class MongoDBRunnable extends JavaBatchRunnable {

	String[] getImports() {
		return new String[] {
			"com.mongodb.DB",
			"com.mongodb.CommandFailureException",
			"com.mongodb.WriteConcernException",
			"com.mongodb.DBCollection",
			"com.mongodb.DBCursor",
			"com.mongodb.BasicDBObject",
			"com.mongodb.BulkWriteOperation"
		};
	};

	final MongoClient mongoClient;
	final DB db;

	public MongoDBRunnable(ThreadContext<String> threadContext) {
		super(threadContext);

		try {
			mongoClient = new MongoClient();
			db = mongoClient.getDB("test2");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void invoke(Method method) throws IllegalAccessException, InvocationTargetException {
		method.invoke(null, threadContext, db);
	}
}
