package de.dhbw.mh.lextream.lexpress.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import de.dhbw.mh.lextream.lexpress.DFA;
import de.dhbw.mh.redeggs.CodePointRange;

class DeterministicStateMachine implements DFA {

	final DFA.State[] states;
	final DFA.State initialState;
	final Set<CodePointRange> symbols;
	final Set<DFA.State> acceptingStates;
	protected final Map<DFA.State, Map<CodePointRange, DFA.State>> transitions;

	DeterministicStateMachine(Collection<DFA.State> states, Set<CodePointRange> symbols, DFA.State initialState, Map<DFA.State, Map<CodePointRange, DFA.State>> transitions, Set<DFA.State> acceptingStates) {
		this(states.toArray(new DFA.State[states.size()]), symbols, initialState, transitions, acceptingStates);
	}

	DeterministicStateMachine(DFA.State[] states, Set<CodePointRange> symbols, DFA.State initialState, Map<DFA.State, Map<CodePointRange, DFA.State>> transitions, Set<DFA.State> acceptingStates) {
		super();
		this.states = states;
		this.symbols = symbols;
		this.initialState = initialState;
		this.transitions = transitions;
		this.acceptingStates = acceptingStates;
	}

	@Override
	public Set<State> getStates() {
		Set<DFA.State> result = new HashSet<>();
		for(DFA.State state : states) {
			result.add(state);
		}
		return result;
	}

	@Override
	public State getInitialState() {
		return initialState;
	}

	@Override
	public Set<State> getAcceptingStates() {
		return acceptingStates;
	}

	@Override
	public Set<CodePointRange> getSymbols() {
		return symbols;
	}

	@Override
	public Map<State, Map<CodePointRange, State>> getTransitions() {
		return transitions;
	}

	@Override
	public void forEachState(Consumer<State> action) {
		for(DFA.State state : states) {
			action.accept(state);
		}
	}

	@Override
	public void forEachAcceptingState(Consumer<State> action) {
		for(DFA.State state : acceptingStates) {
			action.accept(state);
		}
	}

	@Override
	public void forEachTransition(Transition action) {
		for(Map.Entry<DFA.State, Map<CodePointRange, DFA.State>> trans1 : transitions.entrySet()) {
			for(Map.Entry<CodePointRange, DFA.State> trans2 : trans1.getValue().entrySet()) {
				action.accept(trans1.getKey(), trans2.getKey(), trans2.getValue());
			}
		}
	}

	@Override
	public Set<State> findErrorStates() {
		Set<DFA.State> errorStates = new HashSet<>();
		withNextState: for(Map.Entry<DFA.State, Map<CodePointRange, DFA.State>> transition : transitions.entrySet()) {
			DFA.State origin = transition.getKey();
			if(acceptingStates.contains(origin)) {
				continue withNextState;
			}
			for(Map.Entry<CodePointRange, DFA.State> temp : transition.getValue().entrySet()) {
				DFA.State target = temp.getValue();
				if(origin != target) {
					continue withNextState;
				}
			}
			errorStates.add(origin);
		}
		return errorStates;
	}



	static class Builder {
		Set<DFA.State> states;
		Set<CodePointRange> symbols;
		DFA.State initialState;
		Set<DFA.State> acceptingStates;
		protected Map<DFA.State, Map<CodePointRange, DFA.State>> transitions;

		Builder() {
			states = new HashSet<>();
			transitions = new HashMap<>();
			symbols = new HashSet<>();
			acceptingStates = new HashSet<>();
		}

		void initialState(DFA.State initialState){
			this.initialState = initialState;
		}

		void acceptingState(DFA.State acceptingState){
			acceptingStates.add(acceptingState);
		}

		void addTransition(DFA.State origin, CodePointRange symb, DFA.State target){
			Map<CodePointRange, DFA.State> temp = transitions.get(origin);
			if(temp == null) {
				transitions.put(origin, new HashMap<>());
				temp = transitions.get(origin);
			}
			temp.put(symb, target);
			if(!symbols.contains(symb)) {
				symbols.add(symb);
			}
		}

		DFA.State newState() {
			DFA.State state = new DFA.State();
			states.add(state);
			return state;
		}

		DFA finish() {
			DFA.State[] temp = new DFA.State[states.size()];
			int i = 0;
			for(DFA.State state : states) {
				temp[i++] = state;
			}
			return new DeterministicStateMachine(temp, symbols, initialState, transitions, acceptingStates);
		}
	}

}
