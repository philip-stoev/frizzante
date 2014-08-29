package org.stoev.frizzante;

import com.mongodb.MongoClient;
import com.mongodb.DB;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.net.UnknownHostException;

public class MongoDBRunnableFactory implements FuzzRunnableFactory {
	private final String dbName;

	public MongoDBRunnableFactory(final String dbName) {
		this.dbName = dbName;
	}

	public MongoDBRunnableFactory() {
		this.dbName = "test";
	}

	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		return new MongoDBRunnable(runnableManager, threadContext, dbName);
	}
}

class MongoDBRunnable extends JavaBatchRunnable {
	private final MongoClient mongoClient;
	private final DB db;

	MongoDBRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext, final String dbName) {
		super(runnableManager, threadContext);
		try {
			mongoClient = new MongoClient();
			db = mongoClient.getDB(dbName);
		} catch (UnknownHostException unknownHostException) {
			throw new IllegalArgumentException(unknownHostException);
		}
	}

	@Override
	protected String[] getHeaders() {
		return new String[] {
			"import org.stoev.frizzante.ThreadContext",
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
