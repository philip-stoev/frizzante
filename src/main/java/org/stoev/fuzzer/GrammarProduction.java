package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class GrammarProduction implements Generatable {
	private final List<Generatable> elements;

        GrammarProduction(final String productionString) {
                Scanner scanner = new Scanner(productionString);
		elements = new ArrayList<Generatable>();

                while (scanner.hasNext()) {
                        String elementString = scanner.next();
			elements.add(new Literal(elementString));
                }
        }

	GrammarProduction(final List<Generatable> grammarElements) {
		elements = grammarElements;
	}

	public final void generate(final Context context, final StringBuilder buffer) {
		switch (elements.size()) {
			case 0:
				break;
			case 1:
				elements.get(0).generate(context, buffer);
				break;
			default:
				elements.get(0).generate(context, buffer);
				for (int i = 1; i < elements.size(); i++) {
					context.appendUnitSeparator(buffer);
					elements.get(i).generate(context, buffer);
				}
		}
	}

	public final void link(final Grammar grammar) {
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
