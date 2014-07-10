package org.stoev.fuzzer;

import com.mongodb.MongoClient;
import com.mongodb.DB;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.net.UnknownHostException;

public class MongoDBRunnableFactory extends FuzzRunnableFactory {
	private final String dbName;

	public MongoDBRunnableFactory(final String dbName) {
		this.dbName = dbName;
	}

	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) throws UnknownHostException {
		return new MongoDBRunnable(runnableManager, threadContext, dbName);
	}
}

class MongoDBRunnable extends JavaBatchRunnable {
	private final MongoClient mongoClient;
	private final DB db;

	public MongoDBRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext, final String dbName) throws UnknownHostException {
		super(runnableManager, threadContext);
		mongoClient = new MongoClient();
		db = mongoClient.getDB(dbName);
	}

	@Override
	protected String[] getHeaders() {
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

	@Override
	public void invoke(final Method method) throws IllegalAccessException, InvocationTargetException {
		method.invoke(null, db);
	}
}
