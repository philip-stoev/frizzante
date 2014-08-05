package org.stoev.fuzzer;

import com.foundationdb.FDB;
import com.foundationdb.Database;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class FoundationDBRunnableFactory implements FuzzRunnableFactory {
	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		return new FoundationDBRunnable(runnableManager, threadContext);
	}
}

class FoundationDBRunnable extends JavaBatchRunnable {
	private final FDB fdb = FDB.selectAPIVersion(200);
	private final Database db = fdb.open();

	FoundationDBRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		super(runnableManager, threadContext);
	}

	@Override
	protected String[] getHeaders() {
		return new String[] {
			"com.foundationdb.async.Function",
			"com.foundationdb.async.PartialFunction",
			"com.foundationdb.tuple.Tuple",
			"com.foundationdb.Transaction",
			"com.foundationdb.Database",
			"com.foundationdb.async.Future",
		};
	};

	@Override
	public void invoke(final Method method) throws IllegalAccessException, InvocationTargetException {
		method.invoke(null, db);
	}
}
