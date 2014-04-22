package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.IOException;

public class GrammarProduction implements Generatable {
	private final List<Generatable> elements;

        GrammarProduction(final String productionString) {
                Scanner scanner = new Scanner(productionString);
		elements = new ArrayList<Generatable>();

                while (scanner.hasNext()) {
                        String elementString = scanner.next();
			elements.add(new GrammarLiteral(elementString));
                }
        }

	GrammarProduction(final List<Generatable> grammarElements) {
		elements = grammarElements;
	}

	public final void generate(final Context context, final Sentence<?> sentence) throws IOException {
		for (Generatable element: elements) {
			element.generate(context, sentence);
		}
	}

	public final void compile(final Grammar grammar) {
		for (int i = 0; i < elements.size(); i++) {
			String ruleName = elements.get(i).toString();
			Generatable replacement = null;

			if (ruleName.startsWith("$")) {
				String replacementRuleName = ruleName.substring(1);
				replacement = new CachedValue(replacementRuleName);
				grammar.setRuleCached(replacementRuleName);
			} else {
				replacement = grammar.getRule(ruleName);
			}

			if (replacement != null) {
				elements.set(i, replacement);
			}
		}
	}

	public final String toString() {
		assert false;
		return "foo";
	}
}
