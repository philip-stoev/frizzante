package org.stoev.fuzzer;

// import org.testng.Assert;
import org.testng.annotations.Test;

import org.stoev.fuzzer.Grammar.GrammarFlags;
import java.util.EnumSet;

import com.mongodb.MongoClient;
import com.mongodb.DB;

// import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.net.UnknownHostException;

public class MongoTest {
	@Test
	public final void testMongoJava() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, UnknownHostException {
		MongoClient mongoClient = new MongoClient();

		DB db = mongoClient.getDB("test");

		Context<String> c = new Context.ContextBuilder<String>()
			.grammar(Thread.currentThread().getContextClassLoader().getResourceAsStream("mongodb.grammar"), EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
			.build();

		for (int i = 0; i < 100000; i++) {
			String generatedJava = c.generateString();
			StringBuilder javaCode = new StringBuilder();

			if (generatedJava.length() > 65536) {
				continue;
			}

			javaCode.append("package org.stoev.fuzzer;\n");
			javaCode.append("import com.mongodb.DB;\n");
			javaCode.append("import com.mongodb.DBCollection;\n");
			javaCode.append("import com.mongodb.BasicDBObject;\n");

			javaCode.append("public class mongotest {\n");
			javaCode.append("public static void run(DB db) {\n");
			javaCode.append(generatedJava);
	                javaCode.append("}\n");
	                javaCode.append("}\n");

//			Class javaClass = JavaQuickCompile.compile("org.stoev.fuzzer.mongotest", javaCode.toString());
//                      Method javaMethod = javaClass.getDeclaredMethod("run", DB.class);
//                      javaMethod.invoke(null, db);
		}
	}
}
