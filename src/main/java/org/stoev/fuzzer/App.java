package org.stoev.fuzzer;

import org.stoev.fuzzer.Grammar.GrammarFlags;

import java.util.EnumSet;
import java.io.File;

public final class App {
	private App() {	}

	public static void main(final String[] args) throws Throwable {

		int threads = 1;
		int duration = 60;
		long count = Long.MAX_VALUE;
		long range = Long.MAX_VALUE;

		String grammarFile = null;
		EnumSet<GrammarFlags> grammarFlags = EnumSet.noneOf(GrammarFlags.class);
		FuzzRunnableFactory runnableFactory = null;

		for (int i = 0; i < args.length; i++) {
			try {
				switch (args[i]) {
					case "-count":
					case "--count":
						i++;
						count = Long.parseLong(args[i]);
						break;
					case "-duration":
					case "--duration":
						i++;
						duration = Integer.parseInt(args[i]);
						break;
					case "-threads":
					case "--threads":
						i++;
						threads = Integer.parseInt(args[i]);
						break;
					case "-range":
					case "--range":
						i++;
						range = Long.parseLong(args[i]);
						break;
					case "-grammar":
					case "--grammar":
						i++;
						grammarFile = args[i];
						break;
					case "-runnableFactory":
					case "--runnableFactory":
						i++;
						Class<?> runnableFactoryClass = Class.forName(args[i]);
						runnableFactory = (FuzzRunnableFactory) runnableFactoryClass.newInstance();
						break;
					case "-standalone-semicolons":
					case "--standalone-semicolons":
						grammarFlags.add(GrammarFlags.STANDALONE_SEMICOLONS_ONLY);
						break;
					default:
						throw new IllegalArgumentException("Can not parse command line argument '" + args[i] + "'");
				}
			} catch (NumberFormatException numberFormatException) {
				throw new IllegalArgumentException("Can not parse command line argument '" + args[i] + "'", numberFormatException);
			} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
				throw new IllegalArgumentException("Can not parse command line argument '" + args[i - 1] + "'", indexOutOfBoundsException);
			}
		}

		if (grammarFile == null) {
			throw new IllegalArgumentException("-grammar command line argument required");
		}

		if (runnableFactory == null) {
			throw new IllegalArgumentException("-runnableFactory command line argument required");
		}


		final GlobalContext<String> globalContext = new GlobalContext.ContextBuilder<String>()
			.grammar(new File(grammarFile), grammarFlags)
			.idRange(0, range)
			.runnableFactory(runnableFactory)
			.count(count)
			.threads(threads)
			.duration(duration)
			.build();

		globalContext.run();
	}
}
