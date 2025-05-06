package de.dhbw.mh.lextream.lexpress.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.dhbw.mh.lextream.lexport.DfaModel;
import de.dhbw.mh.lextream.lexpress.DFA;

class DfaMapping {

	private static class Labeller {
		private int nextId;
		private final String FORMAT;

		Labeller(String format) {
			super();
			nextId = 0;
			FORMAT = format;
		}

		String nextLabel() {
			return String.format(FORMAT, nextId++);
		}
	}

	static DfaModel mapOntoDfaModel(DFA dfa) {
		Objects.requireNonNull(dfa);
		DfaModel dfaModel = new DfaModel();
		Map<DFA.State, String> stateToId = new HashMap<>();
		Labeller labeller = new Labeller("q%d");
		dfa.forEachState(s -> {
			String label = labeller.nextLabel();
			stateToId.put(s, label);
			dfaModel.addState(label);
		});

		dfaModel.setInitialState(stateToId.get(dfa.getInitialState()));

		dfa.forEachAcceptingState(s -> {
			dfaModel.addAcceptingState(stateToId.get(s));
		});

		dfa.forEachTransition((origin, symbol, target) -> {
			if(symbol == null) {
				dfaModel.addTransition(stateToId.get(origin), null, stateToId.get(target));
			} else {
				String symbolId = dfaModel.getRange(symbol.firstCodePoint, symbol.lastCodePoint);
				dfaModel.addTransition(stateToId.get(origin), symbolId, stateToId.get(target));
			}
		});

		return dfaModel;
	}

}
