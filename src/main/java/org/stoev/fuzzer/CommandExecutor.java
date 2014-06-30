package org.stoev.fuzzer;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandExecutor implements Executor<String> {
	private final String[] envp;
	private final File dir;

	CommandExecutor() {
		this(null, null);
	}

	CommandExecutor(final String[] environment, final File workingDirectory) {
		this.envp = environment;
		this.dir = workingDirectory;
	}

	public final int execute(final Sentence<String> sentence) {
		Runtime runtime = Runtime.getRuntime();

		String[] cmdarray = new String[]{"bash", "-c", sentence.toString()};

		InputStream errorStream = null;
		BufferedReader errorReader = null;

		try {
			Process process = runtime.exec(cmdarray, envp, dir);
			int returnCode = process.waitFor();

			errorStream = process.getErrorStream();
			if (errorStream.available() > 0) {
				errorReader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));

				String errorLine;
//				while ((errorLine = errorReader.readLine()) != null) {
//				}

				if (returnCode == 0) {
					return 1;
				}
			}

			if (returnCode != 0) {
				return returnCode;
			}

			process.destroy();
			return 0;
		} catch (IOException e) {
			return 2;
		} catch (InterruptedException e) {
			return 3;
		} finally {
			try {
				if (errorReader != null) {
					errorReader.close();
				}

				if (errorStream != null) {
					errorStream.close();
				}
			} catch (IOException e) {
				assert false;
			}
		}
	}
}
