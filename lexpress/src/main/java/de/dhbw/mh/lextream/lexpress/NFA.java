package de.dhbw.mh.lextream.lexpress;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import de.dhbw.mh.redeggs.CodePointRange;
import de.dhbw.mh.redeggs.VirtualSymbol;

/**
 * Represents a nondeterministic finite automaton (NFA).
 */
public interface NFA {

	public void forEachState(Consumer<NFA.State> action);

	public void forEachAcceptingState(Consumer<NFA.State> action);

	public void forEachTransition(NFA.Transition action);

	public NFA.State getInitialState();

	public Set<CodePointRange> getAllCodePointRanges();


	/**
	 * Represents a state in the NFA.
	 */
	public class State {
		// Extend or annotate as needed.
	}


	/**
	 * Maps states to numeric identifiers for indexing or serialization.
	 */
	public static class StateNumbering {
		private final Map<State, Integer> stateToId = new LinkedHashMap<>();

		public static StateNumbering forAllStatesOf(NFA nfa) {
			StateNumbering numbering = new StateNumbering();
			var counter = new Object() { int value = 0; };
			nfa.forEachState(state -> numbering.stateToId.put(state, counter.value++));
			return numbering;
		}
		
		public int get(State state) {
			Integer id = stateToId.get(state);
			if(id == null) {
				throw new IllegalArgumentException("State not found in numbering.");
			}
			return id;
		}
	}


	/**
	 * Functional interface for handling transitions in the NFA.
	 */
	@FunctionalInterface
	public interface Transition {
		void accept(NFA.State origin, VirtualSymbol symbol, NFA.State target);
	}

}
