package org.stoev.fuzzer;

import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class JavaBatchCompilerTest {
	@Test
	public final void testImports() throws IllegalAccessException, InvocationTargetException {
		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(null, new String[] {"import java.util.ArrayList"});

		javaCompiler.addJavaClass("TestImports", "public static void testImports() {ArrayList<String> foo = new ArrayList<String>(); }");
		javaCompiler.compileAll();

		for (Class<?> javaClass: javaCompiler) {
			Method[] javaMethods = javaClass.getDeclaredMethods();
			Method javaMethod = javaMethods[0];

			javaMethod.invoke(null);
		}
	}

	@Test
	public final void testPackageDeclaration() throws IllegalAccessException, InvocationTargetException {
		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(null, new String[] {"package foo.bar"});

		javaCompiler.addJavaClass("TestPackageDeclaration", "public static void testPackageDeclaration() {assert true; }");
		javaCompiler.compileAll();

		for (Class<?> javaClass: javaCompiler) {
			Method[] javaMethods = javaClass.getDeclaredMethods();
			Method javaMethod = javaMethods[0];

			javaMethod.invoke(null);
		}
	}


	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testCompilationFailure() throws IllegalAccessException, InvocationTargetException {
		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(null, null);

		javaCompiler.addJavaClass("TestCompilationFailure", "invalid_java");
		javaCompiler.compileAll();
	}

	@Test
	public final void testInternalCaching() throws IllegalAccessException, InvocationTargetException {
		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(null, null);

		javaCompiler.addJavaClass("TestInternalCaching", "public static void testCache() { assert true; }");
		javaCompiler.addJavaClass("TestInternalCaching", "public static void testCache() { assert false; }");
		javaCompiler.compileAll();

		for (Class<?> javaClass: javaCompiler) {
			Method[] javaMethods = javaClass.getDeclaredMethods();
			Method javaMethod = javaMethods[0];

                        javaMethod.invoke(null);
                }
	}

	@Test
	public final void testExternalCaching() throws IllegalAccessException, InvocationTargetException {
		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(JavaBatchCompiler.GLOBAL_CLASS_CACHE, null);

		javaCompiler.addJavaClass("TestExternalCaching", "public static void testCache() { assert true; }");
		javaCompiler.compileAll();
		javaCompiler.addJavaClass("TestExternalCaching", "public static void testCache() { assert false; }");
		javaCompiler.compileAll();

		for (Class<?> javaClass: javaCompiler) {
			Method[] javaMethods = javaClass.getDeclaredMethods();
			Method javaMethod = javaMethods[0];

                        javaMethod.invoke(null);
                }
	}

	@Test (expectedExceptions = InvocationTargetException.class)
	public final void testNoCaching() throws IllegalAccessException, InvocationTargetException {
		JavaBatchCompiler javaCompiler = new JavaBatchCompiler(null, null);

		javaCompiler.addJavaClass("TestNoCaching", "public static void testCache() { assert true; }");
		javaCompiler.compileAll();
		javaCompiler.addJavaClass("TestNoCaching", "public static void testCache() { assert false; }");
		javaCompiler.compileAll();

		for (Class<?> javaClass: javaCompiler) {
			Method[] javaMethods = javaClass.getDeclaredMethods();
			Method javaMethod = javaMethods[0];

                        javaMethod.invoke(null);
                }
	}
}
