package de.dhbw.mh.redeggs;

import java.util.List;

/**
 * Represents a symbolic abstraction over a set of characters or character
 * classes in a regular expression.
 * <p>
 * This interface is typically used to group together characters that behave
 * identically in a regular expression (e.g., digits, letters, or custom
 * ranges). By abstracting these groups into a single {@code VirtualSymbol}, the
 * resulting automaton can avoid generating multiple redundant transitions
 * (e.g., one for each individual character in {@code [a-zA-Z]}).
 * </p>
 *
 * <p>
 * Implementations define how characters map to symbol classes, allowing
 * efficient representation and matching of input during parsing or automaton
 * execution.
 * </p>
 *
 * <p>
 * Examples include digit classes ({@code \d}), word characters ({@code \w}), or
 * user-defined character sets like {@code [aeiou]}.
 * </p>
 */
public interface VirtualSymbol {
	
	public List<CodePointRange> sortedCodePointRanges();

}
