package de.dhbw.mh.lextream.lexpress.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.dhbw.mh.lextream.lexpress.DFA;
import de.dhbw.mh.redeggs.CodePointRange;

class SimpleDfa {

	private final DFA.State[] states;
	private final int initialState;
	private final Set<CodePointRange> symbols;
	private final boolean[] acceptingStates;
	private final Map<Integer, Map<CodePointRange, Integer>> transitions;

	SimpleDfa(DFA dfa) {
		Set<DFA.State> tempStates = dfa.getStates();
		Map<DFA.State, Integer> stateMap = new HashMap<>();
		this.states = new DFA.State[tempStates.size()];
		this.acceptingStates = new boolean[tempStates.size()];
		int i = 0;
		for(DFA.State state : tempStates) {
			this.states[i] = state;
			stateMap.put(state, i);
			acceptingStates[i] = dfa.getAcceptingStates().contains(state);
			++i;
		}
		this.symbols = dfa.getSymbols();
		this.initialState = stateMap.get(dfa.getInitialState());

		this.transitions = new HashMap<>();
		for(Map.Entry<DFA.State,Map<CodePointRange,DFA.State>> transition : dfa.getTransitions().entrySet()) {
			DFA.State origin = transition.getKey();
			int originId = stateMap.get(origin);
			Map<CodePointRange, Integer> temp = new HashMap<>();
			transitions.put(originId, temp);
			for(Map.Entry<CodePointRange,DFA.State> action : transition.getValue().entrySet()) {
				CodePointRange symbol = action.getKey();
				DFA.State target = action.getValue();
				int targetId = stateMap.get(target);
				temp.put(symbol, targetId);
			}
		}
	}

	SimpleDfa(DFA.State[] states, Set<CodePointRange> symbols, int initialState, Map<Integer, Map<CodePointRange, Integer>> transitions, boolean[] acceptingStates) {
		this.states = states;
		this.symbols = symbols;
		this.initialState = initialState;
		this.transitions = transitions;
		this.acceptingStates = acceptingStates;
	}

	public int getInitialState() {
		return initialState;
	}

	public boolean acceptsInState(int id) {
		return acceptingStates[id];
	}

	public int numberOfStates() {
		return states.length;
	}

	public Set<CodePointRange> getSymbols(){
		return symbols;
	}

	public int targetOf(int origin, CodePointRange symbol) {
		return transitions.get(origin).get(symbol);
	}

	public Map<Integer, Map<CodePointRange, Integer>> getTransitions() {
		return transitions;
	}

}
