package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

import org.stoev.fuzzer.GlobalContext.ContextBuilder;

public class VisitorsTest {
	@Test
	public final void testVisitorSimple() {
		class TestVisitor {
			public void foo(final ThreadContext<String> context, final Sentence<String> sentence, final Sentence<String> argument) {
				sentence.add("foo2");
			}

			public void bar(final ThreadContext<String> context, final Sentence<String> sentence, final Sentence<String> argument) {
				sentence.add("bar2");
			}
		}

		Object v = new TestVisitor();
		GlobalContext<String> g = new ContextBuilder<String>().grammar("main: foo bar;").visitor(v).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "foo2 bar2");
	}

	@Test
	public final void testVisitorWithType() {
		class TestObject {

		}

		class TestVisitor {
			public void foo(final ThreadContext<TestObject> context, final Sentence<TestObject> sentence, final Sentence<TestObject> argument) {
				sentence.add(new TestObject());
			}
		}

		Object v = new TestVisitor();
		GlobalContext<TestObject> g = new ContextBuilder<TestObject>().grammar("main: foo;").visitor(v).build();
		ThreadContext<TestObject> c = ThreadContext.newThreadContext(g, 1);

		Sentence<TestObject> sentence = c.newSentence();
		c.generate(sentence);
		Iterator<TestObject> iterator = sentence.iterator();

		Assert.assertTrue(iterator.next() instanceof TestObject);
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testNonvoidVisitor() {
		String grammar = "main: nonvoid;";

		class TestVisitor {
			public boolean nonvoid(final ThreadContext<String> context, final Sentence<String> sentence, final Sentence<String> argument) {
				return true;
			}
		}

		Object v = new TestVisitor();
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).visitor(v).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		c.generateString();
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public final void testInvalidVisitorSignature() {
		String grammar = "main: visitor;";

		class TestVisitor {
			public void visitor() {

			}
		}

		Object v = new TestVisitor();
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).visitor(v).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);
		c.generateString();
	}

	@Test
	public final void testVisitorArguments() {
		String grammar = "main: visitor;\nvisitor: TWO FOUR;";

		class TestVisitor {
			public void visitor(final ThreadContext<String> context, final Sentence<String> sentence, final Sentence<String> argument) {
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
		GlobalContext<String> g = new ContextBuilder<String>().grammar(grammar).visitor(v).build();
		ThreadContext<String> c = ThreadContext.newThreadContext(g, 1);

		Assert.assertEquals(c.generateString(), "ONETWOTHREEFOURFIVE");
	}

	@Test
	public final void testVisitorObjectArguments() {
		String grammar = "#option SKIP_WHITESPACE\nmain: sum;\nsum: one two two one;";

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
			public void sum(final ThreadContext<TestObject> context, final Sentence<TestObject> sentence, final Sentence<TestObject> argument) {
				int sum = 0;

				for (TestObject to: argument) {
					sum = sum + to.getValue();
				}

				sentence.add(new TestObject(sum));
			}

			public void one(final ThreadContext<TestObject> context, final Sentence<TestObject> sentence, final Sentence<TestObject> argument) {
				sentence.add(new TestObject(1));
			}

			public void two(final ThreadContext<TestObject> context, final Sentence<TestObject> sentence, final Sentence<TestObject> argument) {
				sentence.add(new TestObject(2));
			}
		}

		Object v = new TestVisitor();
		GlobalContext<TestObject> g = new ContextBuilder<TestObject>().grammar(grammar).visitor(v).build();
		ThreadContext<TestObject> c = ThreadContext.newThreadContext(g, 1);
		Sentence<TestObject> s = c.newSentence();
		c.generate(s);
		Iterator<TestObject> i = s.iterator();
		Assert.assertEquals(i.next().getValue(), 1 + 2 + 2 + 1);
	}

	// Top-level production is an object visitor, but its arguments are ordinary string productions
	@Test
	public final void testMixedVisitorObjects() {
		String grammar = "#option SKIP_WHITESPACE\nmain: sum;\nsum: 1 2;";

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
			public void sum(final ThreadContext<TestObject> context, final Sentence<TestObject> sentence, final Sentence<String> argument) {
				int sum = 0;

				for (String number: argument) {
					sum = sum + Integer.parseInt(number);
				}

				sentence.add(new TestObject(sum));
			}
		}

		Object v = new TestVisitor();
		GlobalContext<TestObject> g = new ContextBuilder<TestObject>().grammar(grammar).visitor(v).build();
		ThreadContext<TestObject> c = ThreadContext.newThreadContext(g, 1);
		Sentence<TestObject> s = c.newSentence();
		c.generate(s);
		Iterator<TestObject> i = s.iterator();
		Assert.assertEquals(i.next().getValue(), 1 + 2);
	}


	// Arguments of different types
	@Test
	public final void testMixedVisitorArguments() {
		String grammar = "#option SKIP_WHITESPACE\nmain: sum;\nsum: one 2;";

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
			public void sum(final ThreadContext<TestObject> context, final Sentence<TestObject> sentence, final Sentence<?> arguments) {
				int sum = 0;

				for (Object arg: arguments) {
					if (arg.getClass().equals(String.class)) {
						sum = sum + Integer.parseInt((String) arg);
					} else if (arg.getClass().equals(TestObject.class)) {
						TestObject to = (TestObject) arg;
						sum = sum + to.getValue();
					} else {
						Assert.assertTrue(false);
					}
				}

				sentence.add(new TestObject(sum));
			}

			public void one(final ThreadContext<TestObject> context, final Sentence<TestObject> sentence, final Sentence<TestObject> argument) {
				sentence.add(new TestObject(1));
			}
		}

		Object v = new TestVisitor();
		GlobalContext<TestObject> g = new ContextBuilder<TestObject>().grammar(grammar).visitor(v).build();
		ThreadContext<TestObject> c = ThreadContext.newThreadContext(g, 1);
		Sentence<TestObject> s = c.newSentence();
		c.generate(s);
		Iterator<TestObject> i = s.iterator();
		Assert.assertEquals(i.next().getValue(), 1 + 2);
	}



}
