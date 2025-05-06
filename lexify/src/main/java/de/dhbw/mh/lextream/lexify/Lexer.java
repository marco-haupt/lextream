package de.dhbw.mh.lextream.lexify;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.dhbw.mh.lextream.lexport.DfaModel;
import de.dhbw.mh.lextream.lexport.LexerSpecification;
import de.dhbw.mh.redeggs.CodePointRange;

/**
 * Lexer is responsible for creating instances that tokenize input strings
 * using a set of deterministic finite automata.
 */
public class Lexer {

	private final LexicalAutomaton.Instance[] automata;
	private final int[] precedence;
	private final String[] tokenTypes;

	private Lexer(LexicalAutomaton.Instance[] automata, String[] tokenTypes) {
		this.automata = automata;
		this.tokenTypes = tokenTypes;
		this.precedence = latestWins(automata.length);
	}

	private static int[] latestWins(int length) {
		int[] precedence = new int[length];
		for (int i = 0; i < length; i++) {
			precedence[i] = i;
		}
		return precedence;
	}

	/**
	 * Creates a new lexer instance for tokenizing the specified input.
	 * 
	 * @param input The input string to tokenize.
	 * @return A new lexer instance for this input.
	 */
	public Instance newInstance(String input) {
		return new Instance(input);
	}


	/**
	 * Lexer instance that holds state while tokenizing an input string.
	 */
	public class Instance {
		private final String input;
		private int lastAcceptedPosition = -1;
		private int startOfToken = -1;
		private int tokenId = -1;

		private Instance(String input) {
			this.input = input;
		}

		/**
		 * Returns the most recently accepted token.
		 *
		 * @return A Token object representing the matched lexeme.
		 * @throws IllegalStateException if no valid token was accepted
		 */
		public Token getToken() {
			if(tokenId < 0 || tokenId >= tokenTypes.length) {
				String sequence = input.substring(startOfToken, lastAcceptedPosition + 1);
				throw new IllegalStateException(String.format("Unexpected sequence '%s'", sequence));
			}
			return new Token(
					tokenTypes[tokenId],
					input.substring(startOfToken, lastAcceptedPosition + 1),
					startOfToken,
					lastAcceptedPosition + 1
			);
		}


		/**
		 * Checks whether the lexer has finished processing all input characters.
		 * 
		 * @return True if no more characters remain to be processed.
		 */
		public boolean completed() {
			return lastAcceptedPosition + 1 >= input.length();
		}


		/**
		 * Advances the lexer to the next token by consuming input characters
		 * and updating the best-matching token using precedence and automata states.
		 */
		public void advance() {
			int position = startOfToken = lastAcceptedPosition + 1;
			tokenId = -1;

			while(position < input.length()) {
				int codePoint = input.charAt(position);
				boolean anyActive = processCodePoint(codePoint, position);
				if(!anyActive) {
					break;
				}
				++position;
			}

			resetAutomata();
		}


		/**
		 * Processes a single code point across all automata, updating the match if one is found.
		 *
		 * @param codePoint The current character as a code point.
		 * @param position  The current position in the input.
		 * @return true if any automaton is still active, false otherwise
		 */
		private boolean processCodePoint(int codePoint, int position) {
			boolean anyActive = false;

			for (int i = 0; i < automata.length; i++) {
				LexicalAutomaton.Instance automaton = automata[i];
				automaton.consume(codePoint);

				if (automaton.inErrorState()) continue;

				anyActive = true;

				if (!automaton.acceptsInput()) continue;

				if (lastAcceptedPosition == position) {
					if (tokenId == -1 || precedence[tokenId] < precedence[i]) {
						tokenId = i;
					}
				} else {
					tokenId = i;
					lastAcceptedPosition = position;
				}
			}

			return anyActive;
		}


		/**
		 * Resets all automata to their initial state for processing the next token.
		 */
		private void resetAutomata() {
			for (LexicalAutomaton.Instance automaton : automata) {
				automaton.reset();
			}
		}


		// Optional future API
		public void nextToken() {}
		public void peekToken() {}
	}


	/**
	 * Token represents a classified lexeme from the input string.
	 */
	public static class Token {
		public final String type;
		public final String lexeme;
		public final int startOffset;
		public final int endOffset;
		
		private Token(String type, String lexeme, int startOffset, int endOffset) {
			super();
			this.type = type;
			this.lexeme = lexeme;
			this.startOffset = startOffset;
			this.endOffset = endOffset;
		}
	}


	/**
	 * Creates a Lexer from a LexerSpecification.
	 * 
	 * @param definition The lexer specification containing rules.
	 * @return A Lexer instance ready to tokenize input.
	 */
	public static Lexer from(final LexerSpecification definition) {
		LexicalAutomaton.Instance[] automata = new LexicalAutomaton.Instance[definition.getRules().size()];
		String[] tokenTypes = new String[automata.length];
		int i = 0;
		for(LexerSpecification.LexerRule rule : definition.getRules()) {
			automata[i] = automatonFrom(rule.automaton);
			tokenTypes[i] = rule.tokenType;
			++i;
		}
		return new Lexer(automata, tokenTypes);
	}


	private static LexicalAutomaton.Instance automatonFrom(final DfaModel dfaModel){
		return new Builder(dfaModel).build();
	}


	/**
	 * Builds a LexicalAutomaton from a DFA model.
	 */
	private static class Builder {
		private static final int INVALID_SYMBOL = 0;

		private final DfaModel model;
		private final Map<String, Integer> stateMap = new HashMap<>();
		private final Map<String, Integer> symbolMap = new HashMap<>();
		private final SymbolStrategy symbols;
		private final int initialState;
		private final int errorState;
		private final boolean[] acceptingStates;
		private final int[][] transitions;

		private int nextStateId = 0;

		Builder(DfaModel model) {
			this.model = model;

			for(String state : model.getStates()) {
				stateMap.put(state, nextStateId++);
			}

			this.symbols = defineSymbols();
			this.initialState = stateMap.get(model.getInitialState());
			this.errorState = determineErrorState();
			this.acceptingStates = markAcceptingStates();
			this.transitions = new int[nextStateId][];
			defineTransitions();
		}

		LexicalAutomaton.Instance build() {
			LexicalAutomaton automaton = new LexicalAutomaton(initialState, acceptingStates, transitions, errorState, symbols);
			return automaton.newInstance();
		}

		private SymbolStrategy defineSymbols() {
			Map<Integer, CodePointRange> symbIdToRange = new HashMap<>();
			int groupId = 1; // 0 is reserved as ID_OF_INVALID_SYMBOL
			for(Map.Entry<String, CodePointRange> symbol : model.getSymbols().entrySet()) {
				symbolMap.put(symbol.getKey(), groupId);
				symbIdToRange.put(groupId++, symbol.getValue());
			}
			return SymbolStrategy.basedOn(symbIdToRange);
		}

		private int determineErrorState() {
			Set<String> errorStates = model.getErrorStates();
			if(errorStates.size() > 1) {
				throw new IllegalStateException("More than one error state. Minimize the automaton first.");
			}
			return errorStates.isEmpty()
					? nextStateId++
					: stateMap.get(errorStates.iterator().next());
		}

		private boolean[] markAcceptingStates() {
			boolean[] accepting = new boolean[stateMap.size()];
			for (String state : model.getAcceptingStates()) {
				accepting[stateMap.get(state)] = true;
			}
			return accepting;
		}

		private void defineTransitions() {
			for(Map.Entry<String, Map<String, String>> modelTransitions : model.getTransitions().entrySet()) {
				int origin = stateMap.get(modelTransitions.getKey());
				int[] transition = new int[symbols.numberOfGroups()];
				for(Map.Entry<String, String> stateTransition : modelTransitions.getValue().entrySet()) {
					int symbolId = symbolMap.get(stateTransition.getKey());
					int target = stateMap.get(stateTransition.getValue());
					transition[symbolId] = target;
				}
				transition[INVALID_SYMBOL] = errorState;
				transitions[origin] = transition;
			}
		}
	}

}
