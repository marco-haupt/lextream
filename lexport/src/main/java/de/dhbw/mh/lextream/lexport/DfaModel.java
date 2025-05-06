package de.dhbw.mh.lextream.lexport;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dhbw.mh.redeggs.CodePointRange;


/**
 * A plain data container representing a Deterministic Finite Automaton (DFA).
 * 
 * This class is intended for persisting and loading DFA structures from external sources.
 * It encapsulates the automaton's states, symbols, transitions, and other related metadata.
 * It is not responsible for executing or simulating the DFA.
 */
public class DfaModel {

	private final Set<String> states = new HashSet<>();
	private final Map<String, CodePointRange> symbols = new HashMap<>();
	private final Set<String> acceptingStates = new HashSet<>();
	private final Map<String, Map<String, String>> transitions = new HashMap<>();
	private final Set<String> errorStates = new HashSet<>();
	private transient int nextSymbolId = 0;
	
	private String initialState;


	/** Checks whether the DFA model has the required components. */
	public boolean isValid() {
		return !states.isEmpty() && initialState != null;
	}

	/**
	 * Loads a DFA model from its JSON representation.
	 *
	 * @param json the serialized JSON string
	 * @return a DfaModel instance
	 */
	public static DfaModel fromJson(String json) {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.fromJson(json, DfaModel.class);
	}

	public void addState(String state) {
		states.add(state);
	}

	public void setInitialState(String initialState) {
		this.initialState = initialState;
	}

	public void addAcceptingState(String acceptingState) {
		acceptingStates.add(acceptingState);
	}

	public void addTransition(String origin, String symbol, String target) {
		transitions
			.computeIfAbsent(origin, k -> new HashMap<>())
			.put(symbol, target);
	}

	public Set<String> getStates() {
		return Collections.unmodifiableSet(states);
	}

	public String getInitialState() {
		return initialState;
	}

	public Set<String> getAcceptingStates() {
		return Collections.unmodifiableSet(acceptingStates);
	}

	public Map<String, Map<String, String>> getTransitions() {
		return Collections.unmodifiableMap(transitions);
	}

	public Map<String, CodePointRange> getSymbols() {
		return Collections.unmodifiableMap(symbols);
	}

	public Set<String> getErrorStates() {
		return Collections.unmodifiableSet(errorStates);
	}

	/**
	 * Retrieves or assigns a unique symbol label for a code point range.
	 *
	 * @param firstCodepoint start of the Unicode range
	 * @param lastCodepoint  end of the Unicode range
	 * @return symbol label corresponding to the range
	 */
	public String getRange(int firstCodepoint, int lastCodepoint) {
		for(Map.Entry<String, CodePointRange> symbol : symbols.entrySet()) {
			CodePointRange range = symbol.getValue();
			if(firstCodepoint == range.firstCodePoint && lastCodepoint == range.lastCodePoint) {
				return symbol.getKey();
			}
		}
		String label = "s" + nextSymbolId++;
		symbols.put(label, new CodePointRange(firstCodepoint, lastCodepoint));
		return label;
	}

	/**
	 * Serializes the DFA model to JSON.
	 */
	public String asJson() {
		return new Gson().toJson(this);
	}

	/**
	 * Generates a DOT-format string representation of the DFA model
	 * for use with Graphviz or visualization tools.
	 */
	public String asDot() {
		StringBuilder dot = new StringBuilder()
			.append("digraph finite_state_machine {\n")
			.append("  rankdir=LR;\n")
			.append("  node [shape = point]; x;\n");

		if(!acceptingStates.isEmpty()) {
			dot.append("  node [shape = doublecircle];");
			for(String state : acceptingStates) {
				dot.append(" ").append(state);
			}
			dot.append(";\n");
		}

		dot.append("  node [shape = circle];");
		for (String state : states) {
			dot.append(" ").append(state);
		}
		dot.append(";\n");

		dot.append("  x -> ").append(initialState).append(";\n");

		for(Map.Entry<String, Map<String, String>> stateTransitions : this.transitions.entrySet()) {
			String origin = stateTransitions.getKey();
			for(Map.Entry<String, String> stateTransition : stateTransitions.getValue().entrySet()) {
				String symbol = stateTransition.getKey();
				String target = stateTransition.getValue();
				dot.append(String.format("  %s -> %s [ label = \"%s\" ];\n", origin, target, symbol));
			}
		}

		dot.append("}");
		return dot.toString();
	}

}
