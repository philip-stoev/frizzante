package org.stoev.fuzzer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class CommandExecutorTest {
	@Test
	public final void testSuccessfullExecution() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: ls -la\n;")
			.build();

		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);

		Executor<String> executor = new CommandExecutor();

		int result = executor.execute(sentence);
		Assert.assertEquals(result, 0);
	}

	@Test
	public final void testFailedExecution() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: exit 10\n;")
			.build();

		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);

		Executor<String> executor = new CommandExecutor();

		int result = executor.execute(sentence);
		Assert.assertEquals(result, 10);
	}

	@Test
	public final void testBashError() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: no_such_command\n;")
			.build();

		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);

		Executor<String> executor = new CommandExecutor();

		int result = executor.execute(sentence);
		Assert.assertEquals(result, 127);
	}

	@Test
	public final void testStderrExecution() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar("#option STANDALONE_SEMICOLONS\nmain: echo foo 1>&2\n;")
			.build();

		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Sentence<String> sentence = threadContext.newSentence();
		threadContext.generate(sentence);

		Executor<String> executor = new CommandExecutor();

		int result = executor.execute(sentence);
		Assert.assertEquals(result, 1);
	}

	public final void testRealExecution() {
		GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar(Thread.currentThread().getContextClassLoader().getResourceAsStream("filesystem.grammar"))
			.build();

		ThreadContext<String> threadContext = ThreadContext.newThreadContext(globalContext, 1);

		Executor<String> executor = new CommandExecutor(null, new File("/tmp/foo"));

		for (int i = 0; i < 10000; i++) {
			Sentence<String> sentence = threadContext.newSentence();
			threadContext.generate(sentence);
			executor.execute(sentence);
		}
	}
}
