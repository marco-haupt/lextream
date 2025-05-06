package de.dhbw.mh.lextream.lexpress;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import de.dhbw.mh.redeggs.CodePointRange;

/**
 * Represents a deterministic finite automaton (DFA) with states, transitions, and symbol ranges.
 */
public interface DFA {

	public Set<DFA.State> getStates();
	public DFA.State getInitialState();
	public Set<DFA.State> getAcceptingStates();
	public Set<CodePointRange> getSymbols();
	public Map<DFA.State, Map<CodePointRange, DFA.State>> getTransitions();

	public void forEachState(Consumer<DFA.State> action);
	public void forEachAcceptingState(Consumer<DFA.State> action);
	public void forEachTransition(DFA.Transition action);

	public Set<DFA.State> findErrorStates();

	/**
	 * Represents a state in the DFA.
	 */
	public static class State {
		// Extend or annotate as needed.
	}

	/**
	 * Assigns numeric IDs to DFA states for compact indexing or serialization.
	 */
	public static class StateNumbering {
		private final Map<DFA.State, Integer> stateToId;
		private final DFA.State[] idToState;

		public StateNumbering(DFA dfa) {
			int size = dfa.getStates().size();
			stateToId = new LinkedHashMap<>(size);
			idToState = new DFA.State[size];
		}

		public static StateNumbering forAllStatesOf(DFA dfa) {
			StateNumbering numbering = new StateNumbering(dfa);
			int nextId = 0;
			for (State state : dfa.getStates()) {
				numbering.stateToId.put(state, nextId);
				numbering.idToState[nextId] = state;
				++nextId;
			}
			return numbering;
		}

		public int get(State state) {
			return Objects.requireNonNull(stateToId.get(state), "State not found in numbering.");
		}

		public DFA.State get(int id) {
			if (id < 0 || id >= idToState.length) {
				throw new IndexOutOfBoundsException("Invalid state ID: " + id);
			}
			return idToState[id];
		}
	}

	/**
	 * Functional interface for consuming DFA transitions.
	 */
	@FunctionalInterface
	public interface Transition {
		void accept(DFA.State origin, CodePointRange symbol, DFA.State target);
	}

}
