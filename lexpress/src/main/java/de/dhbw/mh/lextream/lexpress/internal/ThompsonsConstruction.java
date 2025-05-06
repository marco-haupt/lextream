package de.dhbw.mh.lextream.lexpress.internal;

import de.dhbw.mh.redeggs.RegularEggspression;
import de.dhbw.mh.redeggs.RegularEggspression.Alternation;
import de.dhbw.mh.redeggs.RegularEggspression.Concatenation;
import de.dhbw.mh.redeggs.RegularEggspression.EmptySet;
import de.dhbw.mh.redeggs.RegularEggspression.EmptyWord;
import de.dhbw.mh.redeggs.RegularEggspression.Literal;
import de.dhbw.mh.redeggs.RegularEggspression.Star;
import de.dhbw.mh.redeggs.VirtualSymbol;

class ThompsonsConstruction implements RegularEggspression.Visitor<RecursiveThompsonAutomaton> {

	@Override
	public RecursiveThompsonAutomaton visit( EmptyWord concat ){
		return new RecursiveThompsonAutomaton.EmptyWord( );
	}

	@Override
	public RecursiveThompsonAutomaton visit( EmptySet concat ){
		return new RecursiveThompsonAutomaton.EmptySet( );
	}

	@Override
	public RecursiveThompsonAutomaton visitPost( Concatenation concat, RecursiveThompsonAutomaton left, RecursiveThompsonAutomaton right ){
		return new RecursiveThompsonAutomaton.Concatenation( left, right );
	}

	@Override
	public RecursiveThompsonAutomaton visitPost( Alternation union, RecursiveThompsonAutomaton left, RecursiveThompsonAutomaton right ){
		return new RecursiveThompsonAutomaton.Alternation(left, right);
	}

	@Override
	public RecursiveThompsonAutomaton visitPost( Star star, RecursiveThompsonAutomaton base ){
		return new RecursiveThompsonAutomaton.KleeneClosure(base);
	}

	@Override
	public RecursiveThompsonAutomaton visitPost( Literal simple, VirtualSymbol symbol ){
		return new RecursiveThompsonAutomaton.Literal( symbol );
	}

}
