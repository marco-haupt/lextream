package de.dhbw.mh.lextream.lexpress.internal;

import java.util.function.Consumer;

import de.dhbw.mh.lextream.lexpress.NFA;

abstract class ThompsonAutomaton implements NFA {

	final NFA.State initialState;
	final NFA.State acceptingState;

	ThompsonAutomaton(NFA.State initialState, NFA.State acceptingState) {
		this.initialState = initialState;
		this.acceptingState = acceptingState;
	}

	@Override
	public void forEachAcceptingState(Consumer<State> action) {
		action.accept(acceptingState);
	}

	@Override
	public State getInitialState() {
		return initialState;
	}

}
