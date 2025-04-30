package de.dhbw.mh.redeggs;

/**
 * A factory interface for creating {@link VirtualSymbol} instances used in
 * regular expression parsing.
 */
public interface SymbolFactory {

	/**
	 * Starts the construction of a new {@link VirtualSymbol}.
	 *
	 * @return a {@link Builder} instance to configure the symbol
	 */
	public Builder newSymbol();

	/**
	 * Builder interface for constructing {@link VirtualSymbol} instances with
	 * custom inclusion and exclusion rules based on character ranges.
	 */
	public static interface Builder {

		/**
		 * Includes the given ranges as part of the symbol.
		 *
		 * @param ranges one or more character ranges to include
		 * @return this builder instance for chaining
		 */
		public Builder include(CodePointRange... ranges);

		/**
		 * Excludes the given ranges from the symbol.
		 *
		 * @param ranges one or more character ranges to exclude
		 * @return this builder instance for chaining
		 */
		public Builder exclude(CodePointRange... ranges);

		/**
		 * Finalizes the symbol creation, allowing no characters outside the defined
		 * include/exclude rules.
		 *
		 * @return the constructed {@link VirtualSymbol}
		 */
		public VirtualSymbol andNothingElse();
	}

}
