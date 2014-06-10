package org.stoev.fuzzer;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.stoev.fuzzer.Grammar.GrammarFlags;
import java.util.EnumSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.mongodb.MongoClient;
import com.mongodb.DB;

import java.util.Random;

import com.foundationdb.FDB;
import com.foundationdb.Database;

import java.io.FileNotFoundException;

public final class App {
/**
 * Not called.
 */
	private App() {

	}

/**
 * main().
 * @param args command-line arguments
 */
	public static void main(final String[] args) throws FileNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		class FoundationDBWorker implements Runnable {

			public void run() {
				try {
				FDB fdb = FDB.selectAPIVersion(200);
				Database db = fdb.open();
				Context<String> c = new Context.ContextBuilder<String>()
					.grammar(new FileInputStream(new File("foundationdb.grammar")), EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
					.random(new Random())
					.build();

				Random random = new Random();
				for (int i = 0; i < 10000; i++) {
					JavaBatchCompiler javaCompiler = new JavaBatchCompiler("org.stoev.fuzzer", new String[] {
						"com.foundationdb.async.Function",
						"com.foundationdb.async.PartialFunction",
						"com.foundationdb.tuple.Tuple",
						"com.foundationdb.Transaction",
						"com.foundationdb.Database",
						"com.foundationdb.async.Future",
						"java.util.*"
					});

					for (int n = 0; n < 100; n++) {
						StringBuilder javaCode = new StringBuilder();
						String generatedJava = c.generateString();
						javaCompiler.addJava("class" + n, generatedJava);
					}

					long compilationStart = System.nanoTime();
					javaCompiler.compileAll();
					long compilationEnd = System.nanoTime();
//					System.out.println("Compilation took " + ((compilationEnd - compilationStart) / 1000000) + " ms.");
	
					for (Iterator<Class<?>> iterator = javaCompiler.iterator(); iterator.hasNext();) {
						Class<?> javaClass = iterator.next();
						Method javaMethod = javaClass.getDeclaredMethod("run", Database.class, Random.class);
						javaMethod.invoke(null, db, random);
					}
				}}
				catch (Exception e) { assert false; };
			}

			public void start () {
				Thread t = new Thread (this);
				t.start();
			}
		}

		for (int i = 0; i < 25; i++) {
			FoundationDBWorker worker = new FoundationDBWorker();
			worker.start();
		}
	}

	public static void mainMongo(final String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
		MongoClient mongoClient = new MongoClient();

		DB db = mongoClient.getDB("test");

		Context<String> c = new Context.ContextBuilder<String>()
			.grammar(new FileInputStream(new File("mongodb.grammar")), EnumSet.of(GrammarFlags.STANDALONE_SEMICOLONS_ONLY))
			.random(new Random())
			.build();

		for (int i = 0; i < 1000; i++) {
			JavaBatchCompiler javaCompiler = new JavaBatchCompiler("org.stoev.fuzzer", new String[] {
				"com.mongodb.DB",
				"com.mongodb.CommandFailureException",
				"com.mongodb.WriteConcernException",
				"com.mongodb.DBCollection",
				"com.mongodb.DBCursor",
				"com.mongodb.BasicDBObject",
				"com.mongodb.BulkWriteOperation"
			});

			for (int n = 0; n < 100; n++) {
				StringBuilder javaCode = new StringBuilder();
				String generatedJava = c.generateString();

				javaCode.append("public static void run(DB db) {\n");
				javaCode.append(generatedJava);
		                javaCode.append("}\n");

				javaCompiler.addJava("class" + n, javaCode.toString());
			}

			long compilationStart = System.nanoTime();
			javaCompiler.compileAll();
			long compilationEnd = System.nanoTime();
			System.out.println("Compilation took " + ((compilationEnd - compilationStart) / 1000000) + " ms.");

			for (Iterator<Class<?>> iterator = javaCompiler.iterator(); iterator.hasNext();) {
				Class<?> javaClass = iterator.next();
				Method javaMethod = javaClass.getDeclaredMethod("run", DB.class);
				javaMethod.invoke(null, db);
			}
		}
	}

	public static void mainsimple(final String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		StringBuilder javaCode = new StringBuilder();

		javaCode.append("package org.stoev.fuzzer;\n");
		javaCode.append("public class foo {\n");
		javaCode.append("public static String one() {\n");
                javaCode.append("return \"one\";\n");
                javaCode.append("}\n");
                javaCode.append("}\n");

		long start = System.nanoTime();
		long preventOptimization = 0;
		final long millispernano = 1000000;

		System.out.println(javaCode.toString());

//		for (int i = 1; i <= 1000; i = i + 1) {
//			Class javaClass = JavaQuickCompile.compile("org.stoev.fuzzer.foo", javaCode.toString());
//			Method javaMethod = javaClass.getDeclaredMethod("one");
//			String output = (String) javaMethod.invoke(null);
//			assert output.equals("one");
//			preventOptimization = preventOptimization + output.length();
//		}

		long end = System.nanoTime();
		System.out.println("Benchmark took " + ((end - start) / millispernano) + " msecs. " + preventOptimization);

	}

	public static void mainBenchmark2(final String[] args) {

		String grammar = "main: good1 | sometimes | bad1 ; sometimes: good2 | bad2 ;";
		Context<String> context = new Context.ContextBuilder<String>().grammar(grammar).build();

		final long iterations = 1000;
		final long cycles = 10;
		final float coefficient = 0.1f;

		for (int c = 1; c <= cycles; c = c + 1) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			for (int i = 1; i <= iterations; i = i + 1) {
				Sentence<String> sentence = context.newSentence();
				context.generate(sentence);
				String generated = sentence.toString();

				if (map.containsKey(generated)) {
					map.put(generated, map.get(generated) + 1);
				} else {
					map.put(generated, 1);
				}

				if (generated.contains("good")) {
					assert true;
					sentence.succeeded(coefficient);
				} else if (generated.contains("bad")) {
					sentence.failed(coefficient);
				}
			}

			System.out.println("Cycle: " + c);
			System.out.println(map.toString());
			System.out.println(grammar);
			System.out.println();
		}
	}

	public static void mainBenchmark(final String[] args) {

		String grammar = "main: foo , main | foo , foo ; foo: foo1 | foo2 ; foo2.java: { sentence.add(\"foo4\"); };";
		Context<String> context = new Context.ContextBuilder<String>().grammar(grammar).build();

		final long iterations = 10000000;
		final long millispernano = 1000000;

		long start = System.nanoTime();
		long preventOptimization = 0;

		for (int x = 1; x <= iterations; x = x + 1) {
			String sentence = context.generateString();
			preventOptimization = preventOptimization + sentence.length();
		}

		long end = System.nanoTime();

		System.out.println("Benchmark took " + ((end - start) / millispernano) + " msecs. " + preventOptimization);
	}
}
