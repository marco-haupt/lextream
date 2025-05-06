package de.dhbw.mh.lextream.lexify;

public class LexicalAutomaton {

	private final int initialState;
	private final int errorState;
	private final boolean[] acceptingStates;
	private final SymbolStrategy symbols;
	private final int[][] transitions;

	public LexicalAutomaton(int initialState, boolean[] acceptingStates, int[][] transitions, int errorState, SymbolStrategy symbols) {
		this.initialState = initialState;
		this.acceptingStates = acceptingStates;
		this.transitions = transitions;
		this.errorState = errorState;
		this.symbols = symbols;
	}

	public Instance newInstance() {
		return new Instance();
	}


	/**
	 * A runtime instance of the automaton which can consume input and track state.
	 */
	public class Instance {
		private int state;

		private Instance() {
			state = initialState;
		}

		public void reset() {
			state = initialState;
		}

		public boolean inErrorState() {
			return state == errorState;
		}

		public boolean acceptsInput() {
			return acceptingStates[state];
		}

		public void consume(int codePoint) {
			if(inErrorState()) {
				return;
			}
			try {
				int symbolId = symbols.containing(codePoint);
				state = transitions[state][symbolId];
			}catch(ArrayIndexOutOfBoundsException ex) {
				state = errorState;
			}
		}
	}

}
