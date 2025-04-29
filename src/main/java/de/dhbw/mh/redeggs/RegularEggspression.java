package de.dhbw.mh.redeggs;

/**
 * Represents a regular expression in an abstract syntax tree (AST) format.
 * Subclasses of this class define different types of regular expression
 * constructs such as concatenation, alternation, and literals.
 */
public abstract class RegularEggspression {

	/**
	 * A visitor interface for traversing and processing different types of regular
	 * expression nodes.
	 *
	 * @param <T> the return type of the visit methods
	 */
	public static interface Visitor<T> {

		/**
		 * Visits a concatenation node.
		 *
		 * @param concat the concatenation node to visit
		 * @return the result of visiting the node
		 */
		public default T visit(RegularEggspression.Concatenation concat) {
			T prefix = concat.left.accept(this);
			T suffix = concat.right.accept(this);
			return visitPost(concat, prefix, suffix);
		}

		/**
		 * Visits an alternation node.
		 *
		 * @param union the alternation node to visit
		 * @return the result of visiting the node
		 */
		public default T visit(RegularEggspression.Alternation union) {
			T first = union.left.accept(this);
			T second = union.right.accept(this);
			return visitPost(union, first, second);
		}

		/**
		 * Visits a star node.
		 *
		 * @param star the star node to visit
		 * @return the result of visiting the node
		 */
		public default T visit(RegularEggspression.Star star) {
			T base = star.base.accept(this);
			return visitPost(star, base);
		}

		/**
		 * Visits a literal node.
		 *
		 * @param literal the literal node to visit
		 * @return the result of visiting the node
		 */
		public default T visit(RegularEggspression.Literal literal) {
			return visitPost(literal, literal.symbol);
		}

		/**
		 * Visits an empty word node.
		 *
		 * @param concat the empty word node to visit
		 * @return the result of visiting the node
		 */
		public T visit(RegularEggspression.EmptyWord concat);

		/**
		 * Visits an empty set node.
		 *
		 * @param concat the empty set node to visit
		 * @return the result of visiting the node
		 */
		public T visit(RegularEggspression.EmptySet concat);

		/**
		 * Post-visit processing for concatenation nodes.
		 *
		 * @param concat the concatenation node
		 * @param prefix the result of visiting the left operand
		 * @param suffix the result of visiting the right operand
		 * @return the result of post-processing
		 */
		public T visitPost(RegularEggspression.Concatenation concat, T prefix, T suffix);

		/**
		 * Post-visit processing for alternation nodes.
		 *
		 * @param union  the alternation node
		 * @param first  the result of visiting the left operand
		 * @param second the result of visiting the right operand
		 * @return the result of post-processing
		 */
		public T visitPost(RegularEggspression.Alternation union, T first, T second);

		/**
		 * Post-visit processing for star nodes.
		 *
		 * @param star the star node
		 * @param base the result of visiting the base operand
		 * @return the result of post-processing
		 */
		public T visitPost(RegularEggspression.Star star, T base);

		/**
		 * Post-visit processing for literal nodes.
		 *
		 * @param literal the literal node
		 * @param symbol  the virtual symbol of the literal
		 * @return the result of post-processing
		 */
		public T visitPost(RegularEggspression.Literal literal, VirtualSymbol symbol);
	}

	/**
	 * Accepts a visitor to process this regular expression.
	 *
	 * @param visitor the visitor that processes this expression
	 * @param <T>     the return type of the visitor's methods
	 * @return the result of the visitor's processing
	 */
	public abstract <T> T accept(RegularEggspression.Visitor<T> visitor);

	/**
	 * Represents a concatenation of two regular expressions.
	 */
	public static class Concatenation extends RegularEggspression {
		private final RegularEggspression left, right;

		/**
		 * Constructs a new concatenation of two regular expressions.
		 *
		 * @param left  the left operand of the concatenation
		 * @param right the right operand of the concatenation
		 */
		public Concatenation(RegularEggspression left, RegularEggspression right) {
			super();
			this.left = left;
			this.right = right;
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visit(this);
		}

		@Override
		public String toString() {
			return String.format("(%s%s)", left, right);
		}
	}

	/**
	 * Represents an alternation (OR) of two regular expressions.
	 */
	public static class Alternation extends RegularEggspression {
		private final RegularEggspression left, right;

		/**
		 * Constructs a new alternation (OR) between two regular expressions.
		 *
		 * @param left  the left operand of the alternation
		 * @param right the right operand of the alternation
		 */
		public Alternation(RegularEggspression left, RegularEggspression right) {
			super();
			this.left = left;
			this.right = right;
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visit(this);
		}

		@Override
		public String toString() {
			return String.format("(%s|%s)", left, right);
		}
	}

	/**
	 * Represents a repetition (Kleene star) of a regular expression.
	 */
	public static class Star extends RegularEggspression {
		private final RegularEggspression base;

		/**
		 * Constructs a new star (Kleene star) operation on the base regular expression.
		 *
		 * @param base the regular expression to repeat
		 */
		public Star(RegularEggspression base) {
			super();
			this.base = base;
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visit(this);
		}

		@Override
		public String toString() {
			return String.format("(%s)*", base);
		}
	}

	/**
	 * Represents a literal (symbol) in the regular expression.
	 */
	public static class Literal extends RegularEggspression {
		private final VirtualSymbol symbol;

		/**
		 * Constructs a new literal regular expression with the given symbol.
		 *
		 * @param virtualSymbol the symbol representing the literal
		 */
		public Literal(VirtualSymbol virtualSymbol) {
			super();
			this.symbol = virtualSymbol;
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visit(this);
		}

		@Override
		public String toString() {
			return String.format("%s", symbol);
		}
	}

	/**
	 * Represents the empty word (epsilon) in the regular expression.
	 */
	public static class EmptyWord extends RegularEggspression {
		public EmptyWord() {
			super();
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visit(this);
		}
	}

	/**
	 * Represents the empty set (no valid matches) in the regular expression.
	 */
	public static class EmptySet extends RegularEggspression {
		public EmptySet() {
			super();
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visit(this);
		}
	}

}
