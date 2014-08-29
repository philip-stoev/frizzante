package org.stoev.fuzzer;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class JDBCRunnableFactory implements FuzzRunnableFactory {
	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		return new JDBCRunnable(runnableManager, threadContext);
	}
}

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
class JDBCRunnable extends FuzzRunnable {
	private static final int ER_LOCK_WAIT_TIMEOUT = 1205;

	private Connection connection;
	private Statement statement;

	JDBCRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		super(runnableManager, threadContext);

		try {
			final String jdbcURL = System.getProperty("jdbc.url", "jdbc:mysql://127.0.0.1:3306/test");
			final String jdbcUsername = System.getProperty("jdbc.username", "root");
			final String jdbcPassword = System.getProperty("jdbc.password", "");

			connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
			statement = connection.createStatement();
		} catch (SQLException sqlException) {
			runtimeException(new IllegalArgumentException(sqlException));
		}
	}

	@Override
	public final void execute(final Sentence<?> sentence) {
		try {
			statement.execute(sentence.toString());
		} catch (MySQLIntegrityConstraintViolationException mysqlIntegrityConstraintViolationException) {
			// Do nothing here as integrity constraint violations are to be expected during fuzz testing
			assert true;
		} catch (SQLException sqlException) {
			switch (sqlException.getErrorCode()) {
				case ER_LOCK_WAIT_TIMEOUT:
					// Lock wait timeout is also expected
					break;
				default:
					executionException(sqlException.getMessage(), sqlException, sentence);
			}
		}
	}
}
