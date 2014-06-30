package org.stoev.fuzzer;

import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class JavaBatchCompilerTest {
	@Test
	public final void testImports() throws IllegalAccessException, InvocationTargetException {
		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(false, "org.stoev.fuzzer", "testImports", null, new String[] {"java.util.ArrayList"});

		javaCompiler.addJava("TestImports", "public static void testImports() {ArrayList<String> foo = new ArrayList<String>(); }");
		javaCompiler.compileAll();

		for (Method javaMethod: javaCompiler) {
			javaMethod.invoke(null);
		}
	}

	@Test
	public final void testCache1() throws IllegalAccessException, InvocationTargetException {
		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(true, "org.stoev.fuzzer", "testCache", null, null);

		javaCompiler.addJava("TestCache1", "public static void testCache() { assert true; }");
		javaCompiler.addJava("TestCache1", "public static void testCache() { assert false; }");
		javaCompiler.compileAll();

                for (Method javaMethod: javaCompiler) {
                        javaMethod.invoke(null);
                }
	}

	@Test
	public final void testCache2() throws IllegalAccessException, InvocationTargetException {
		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(true, "org.stoev.fuzzer", "testCache", null, null);

		javaCompiler.addJava("TestCache2", "public static void testCache() { assert true; }");
		javaCompiler.compileAll();
		javaCompiler.addJava("TestCache2", "public static void testCache() { assert false; }");
		javaCompiler.compileAll();

                for (Method javaMethod: javaCompiler) {
                        javaMethod.invoke(null);
                }
	}
}
