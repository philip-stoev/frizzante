package org.stoev.frizzante;

import org.python.util.PythonInterpreter;

public class PythonRunnableFactory implements FuzzRunnableFactory {
	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public FuzzRunnable newRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		return new PythonRunnable(runnableManager, threadContext);
	}
}

class PythonRunnable extends FuzzRunnable {
	private final PythonInterpreter pythonInterpreter = new PythonInterpreter();

	PythonRunnable(final RunnableManager runnableManager, final ThreadContext<?> threadContext) {
		super(runnableManager, threadContext);
	}

	@Override
	public final void execute(final Sentence<?> sentence) {
		pythonInterpreter.exec(sentence.toString());
	}
}
