package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.EnumSet;

import org.stoev.fuzzer.Context.ContextBuilder;
import org.stoev.fuzzer.Grammar.GrammarFlags;

public class VisitorsTest {
	@Test
	public final void testVisitorSimple() {
		class TestVisitor {
			public void foo(final Context context, final Sentence<String> sentence, final Sentence<String> argument) {
				sentence.add("foo2");
			}

			public void bar(final Context context, final Sentence<String> sentence, final Sentence<String> argument) {
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
			public void foo(final Context context, final Sentence<TestObject> sentence, final Sentence<TestObject> argument) {
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
			public boolean nonvoid(final Context context, final Sentence<String> sentence, final Sentence<String> argument) {
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

	@Test
	public final void testVisitorArguments() {
		String g = "main: visitor;\nvisitor: TWO FOUR;";

		class TestVisitor {
			public void visitor(final Context context, final Sentence<String> sentence, final Sentence<String> argument) {
				sentence.append("ONE");
				Iterator<String> i = argument.iterator();
				sentence.append(i.next());
				sentence.append("THREE");
				i.next(); // Eat the space
				sentence.append(i.next());
				sentence.append("FIVE");
			}
		}

		Object v = new TestVisitor();
		Context c = new ContextBuilder().grammar(g).visitor(v).build();
		Assert.assertEquals(c.generateString(), "ONETWOTHREEFOURFIVE");
	}

	@Test
	public final void testVisitorObjectArguments() {
		String g = "main: sum;\nsum: one two two one;";

		class TestObject {
			private int value;
			TestObject(final int v) {
				value = v;
			}
			int getValue() {
				return value;
			}
		}

		class TestVisitor {
			public void sum(final Context context, final Sentence<TestObject> sentence, final Sentence<TestObject> argument) {
				int sum = 0;

				for (TestObject to: argument) {
					sum = sum + to.getValue();
				}

				sentence.add(new TestObject(sum));
			}

			public void one(final Context context, final Sentence<TestObject> sentence, final Sentence<TestObject> argument) {
				sentence.add(new TestObject(1));
			}

			public void two(final Context context, final Sentence<TestObject> sentence, final Sentence<TestObject> argument) {
				sentence.add(new TestObject(2));
			}
		}

		Object v = new TestVisitor();
		Context c = new ContextBuilder().grammar(g, EnumSet.of(GrammarFlags.SKIP_WHITESPACE)).visitor(v).build();
		Sentence<TestObject> s = new Sentence<TestObject>();
		c.generate(s);
		Iterator<TestObject> i = s.iterator();
		Assert.assertEquals(i.next().getValue(), 1 + 2 + 2 + 1);
	}

}
