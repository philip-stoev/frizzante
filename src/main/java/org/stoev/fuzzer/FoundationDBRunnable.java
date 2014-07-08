package org.stoev.fuzzer;

import com.foundationdb.FDB;
import com.foundationdb.Database;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public final class FoundationDBRunnable extends JavaBatchRunnable {

	protected String[] getImports() {
		return new String[] {
			"com.foundationdb.async.Function",
			"com.foundationdb.async.PartialFunction",
			"com.foundationdb.tuple.Tuple",
			"com.foundationdb.Transaction",
			"com.foundationdb.Database",
			"com.foundationdb.async.Future",
		};
	};

	private final FDB fdb = FDB.selectAPIVersion(200);
	private final Database db = fdb.open();

	public FoundationDBRunnable(final RunnableManager runnableManager, final ThreadContext<String> threadContext) {
		super(runnableManager, threadContext);
	}

	public void invoke(final Method method) throws IllegalAccessException, InvocationTargetException {
		method.invoke(null, threadContext, db);
	}
}
