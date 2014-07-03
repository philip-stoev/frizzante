package org.stoev.fuzzer;

import com.foundationdb.FDB;
import com.foundationdb.Database;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class FoundationDBRunnable extends JavaBatchRunnable {

	String[] getImports() {
		return new String[] {
			"com.foundationdb.async.Function",
			"com.foundationdb.async.PartialFunction",
			"com.foundationdb.tuple.Tuple",
			"com.foundationdb.Transaction",
			"com.foundationdb.Database",
			"com.foundationdb.async.Future",
		};
	};

	final FDB fdb = FDB.selectAPIVersion(200);
	final Database db = fdb.open();

	public FoundationDBRunnable(ThreadContext<String> threadContext) {
		super(threadContext);
	}

	public void invoke(Method method) throws IllegalAccessException, InvocationTargetException {
		method.invoke(null, threadContext, db);
	}
}
