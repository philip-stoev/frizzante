package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

import org.stoev.fuzzer.Context.ContextBuilder;

public class VisitorsTest {
	@Test
	public final void testVisitorSimple() {
		class TestVisitor {
			public void foo(final Context context, final Sentence<String> sentence) {
				sentence.add("foo2");
			}

			public void bar(final Context context, final Sentence<String> sentence) {
				sentence.add("bar2");
			}
		}

		Object v = new TestVisitor();
		Context c = new ContextBuilder().grammar("main: foo bar;").visitor(v).build();

		Assert.assertEquals(c.generateString(), "foo2 bar2");
	}

	@Test
	public final void testVisitorWithType() {
		class TestObject {

		}

		class TestVisitor {
			public void foo(final Context context, final Sentence<TestObject> sentence) {
				sentence.add(new TestObject());
			}
		}

		Object v = new TestVisitor();
		Context c = new ContextBuilder().grammar("main: foo;").visitor(v).build();

		Sentence<TestObject> sentence = new Sentence<TestObject>();
		c.generate(sentence);
		Iterator<TestObject> iterator = sentence.iterator();

		Assert.assertTrue(iterator.next() instanceof TestObject);
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testNonvoidVisitor() {
		String g = "main: nonvoid;";

		class TestVisitor {
			public boolean nonvoid(final Context context, final Sentence<String> sentence) {
				return true;
			}
		}

		Object v = new TestVisitor();
		Context c = new ContextBuilder().grammar(g).visitor(v).build();
		c.generateString();
	}

	@Test (expectedExceptions = ConfigurationException.class)
	public final void testInvalidVisitorSignature() {
		String g = "main: visitor;";

		class TestVisitor {
			public void visitor() {

			}
		}

		Object v = new TestVisitor();
		Context c = new ContextBuilder().grammar(g).visitor(v).build();
		c.generateString();
	}
}
