package de.dhbw.mh.lextream.lexport;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

/**
 * Defines a lexer specification composed of multiple DFA-based rules.
 */
public class LexerSpecification {

	private final Set<LexerRule> rules = new HashSet<>();

	/**
	 * Returns the unmodifiable set of lexer rules.
	 */
	public Set<LexerRule> getRules() {
		return Collections.unmodifiableSet(rules);
	}

	/**
	 * Adds a new rule to the lexer specification.
	 *
	 * @param automaton the DFA model used to recognize the token
	 * @param tokenType the name of the token type
	 * @throws IllegalArgumentException if tokenType is null or blank
	 */
	public void addRule(DfaModel automaton, String tokenType) {
		if (tokenType == null || tokenType.isBlank()) {
			throw new IllegalArgumentException("Token type must not be null or blank.");
		}
		rules.add(new LexerRule(automaton, tokenType));
	}

	/**
	 * Validates all lexer rules in the specification.
	 *
	 * @return true if all rules are valid, false otherwise
	 */
	public boolean isValid() {
		return rules.stream().allMatch(rule ->
			rule.automaton.isValid() && rule.tokenType != null && !rule.tokenType.isBlank()
		);
	}

	/**
	 * Serializes this specification to JSON.
	 */
	public String toJson() {
		return new Gson().toJson(this);
	}

	/**
	 * Serializes this specification to JSON.
	 */
	public String asJson() {
		return new Gson().toJson(this);
	}

	/**
	 * Deserializes a lexer specification from JSON.
	 *
	 * @param json the JSON representation of a LexerSpecification
	 * @return the parsed LexerSpecification
	 */
	public static LexerSpecification fromJson(String json) {
		return new Gson().fromJson(json, LexerSpecification.class);
	}

	/**
	 * Represents a single tokenization rule: a DFA model and its token type.
	 */
	public static class LexerRule {
		public final DfaModel automaton;
		public final String tokenType;

		public LexerRule(DfaModel automaton, String tokenType) {
			this.automaton = automaton;
			this.tokenType = tokenType;
		}
	}

}
