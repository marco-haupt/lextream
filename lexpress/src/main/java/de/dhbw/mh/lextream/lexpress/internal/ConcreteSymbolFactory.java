package de.dhbw.mh.lextream.lexpress.internal;

import java.util.LinkedList;
import java.util.List;

import de.dhbw.mh.redeggs.CodePointRange;
import de.dhbw.mh.redeggs.SymbolFactory;
import de.dhbw.mh.redeggs.VirtualSymbol;

public class ConcreteSymbolFactory implements SymbolFactory {

	@Override
	public Builder newSymbol() {
		return new SymbolFactory.Builder() {

			public List<CodePointRange> ranges = new LinkedList<>();

			@Override
			public Builder include(CodePointRange... extras) {
				for (CodePointRange range : extras) {
					ranges.add(range);
				}
				return this;
			}

			@Override
			public Builder exclude(CodePointRange... extras) {
				throw new RuntimeException("not yet supported");
			}

			@Override
			public VirtualSymbol andNothingElse() {
				return new CompositeSymbol(ranges);
			}

		};
	}

}
