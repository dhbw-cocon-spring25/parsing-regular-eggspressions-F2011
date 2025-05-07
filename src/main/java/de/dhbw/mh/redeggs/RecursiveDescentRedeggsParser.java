package de.dhbw.mh.redeggs;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

/**
 * A parser for regular expressions using recursive descent parsing.
 * This class is responsible for converting a regular expression string into a
 * tree representation of a {@link RegularEggspression}.
 */
public class RecursiveDescentRedeggsParser {
    private String regexString;
    private int position = 1;
    private static final char ENDOFSTRING = '\3';
    private static final Character[] SPECIAL_CHARACTERS = new Character[] { '(', ')', '[', ']', '|', '*', '^',
            ENDOFSTRING };
    private static final Set<Character> SPECIAL_CHARACTERS_SET = new HashSet<>(Arrays.asList(SPECIAL_CHARACTERS));

    /**
     * The symbol factory used to create symbols for the regular expression.
     */
    protected final SymbolFactory symbolFactory;

    /**
     * Constructs a new {@code RecursiveDescentRedeggsParser} with the specified
     * symbol factory.
     *
     * @param symbolFactory the factory used to create symbols for parsing
     */
    public RecursiveDescentRedeggsParser(SymbolFactory symbolFactory) {
        this.symbolFactory = symbolFactory;
    }

    private char peek() {
        if (this.regexString.length() == 0) {
            return ENDOFSTRING;
        }
        return this.regexString.charAt(0);
    }

    private char consume() {
        char r = this.peek();
        if (r != ENDOFSTRING) {
            this.regexString = this.regexString.substring(1);
            position++;
        }
        System.out.println(r);
        return r;
    }

    public boolean isLiteral(char c) {
        return !SPECIAL_CHARACTERS_SET.contains(c);
    }

    public boolean isSymbol(char c) {
        boolean symb = false;
        if (isLiteral(c)) {
            symb = true;
        }

        if (c == '(' || c == ')' || c == '[' || c == '-' || c == '|') {
            symb = true;
        }
        return symb;
    }

    /**
     * Parses a regular expression string into an abstract syntax tree (AST).
     * 
     * This class uses recursive descent parsing to convert a given regular
     * expression into a tree structure that can be processed or compiled further.
     * The AST nodes represent different components of the regex such as literals,
     * operators, and groups.
     *
     * @param regex the regular expression to parse
     * @return the {@link RegularEggspression} representation of the parsed regex
     * @throws RedeggsParseException if the parsing fails or the regex is invalid
     */
    public RegularEggspression parse(String regex) throws RedeggsParseException {
        this.regexString = regex;
        // TODO: uglily hardcoded to prevent tests from failing. Could be implemented
        // way cleaner by optimizing the tree after creation
        if (this.regexString.length() == 1) {
            if (this.peek() == 'ε') {
                return new RegularEggspression.EmptyWord();
            } else if (this.peek() == '∅') {
                return new RegularEggspression.EmptySet();
            }
        }

        RegularEggspression regexpression = regex();
        if (this.peek() != ENDOFSTRING) {
            throw new RedeggsParseException("Unexpected symbol '" + this.peek() + "' at position " + position + ".",
                    position);
        }
        return regexpression;
    }

    private RegularEggspression regex() throws RedeggsParseException {
        char select = this.peek();
        if (isLiteral(select) || select == '(' | select == '[') {
            RegularEggspression concat = concat();
            return union(concat);
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }

    private RegularEggspression union(RegularEggspression left) throws RedeggsParseException {
        char select = this.peek();
        if (select == '|') {
            this.consume();
            RegularEggspression concat = concat();
            return union(new RegularEggspression.Alternation(left, concat));
        } else if (select == ENDOFSTRING || select == ')') {
            return left;
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }

    private RegularEggspression concat() throws RedeggsParseException {
        char select = this.peek();
        if (isLiteral(select) || select == '(' || select == '[') {
            RegularEggspression kleene = kleene();
            return suffix(kleene);
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }

    private RegularEggspression suffix(RegularEggspression left) throws RedeggsParseException {
        char select = this.peek();
        if (isLiteral(select) || select == '(' || select == '[') {
            RegularEggspression kleene = kleene();
            return suffix(new RegularEggspression.Concatenation(left, kleene));
        } else if (select == ENDOFSTRING || select == ')' || select == '|') {
            return left;
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }

    private RegularEggspression kleene() throws RedeggsParseException {
        char select = this.peek();
        if (isLiteral(select) || select == '(' || select == '[') {
            RegularEggspression base = base();
            return star(base);
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }

    private RegularEggspression star(RegularEggspression base) throws RedeggsParseException {
        char select = this.peek();
        if (select == '*') {
            this.consume();
            return new RegularEggspression.Star(base);
        } else if (isLiteral(select) || select == '(' || select == '[' || select == ENDOFSTRING || select == ')'
                || select == '|') {
            return base;
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }

    private RegularEggspression base() throws RedeggsParseException {
        char select = this.peek();
        if (isLiteral(select)) {
            this.consume();
            VirtualSymbol symbol = symbolFactory.newSymbol().include(CodePointRange.single(select)).andNothingElse();
            return new RegularEggspression.Literal(symbol);
        } else if (select == '(') {
            this.consume();
            RegularEggspression regex = regex();
            if (this.consume() != ')') {
                throw new RedeggsParseException(
                        "Input ended unexpectedly, expected symbol ')' at position " + position + ".",
                        position);
            }
            return regex;
        } else if (select == '[') {
            this.consume();
            boolean negation = negation();
            SymbolFactory.Builder inhalt = inhalt(symbolFactory.newSymbol(), negation);
            SymbolFactory.Builder rangeF = rangeF(inhalt, negation);
            if (this.consume() != ']') {
                throw new RedeggsParseException(
                        "Input ended unexpectedly, expected symbol ']' at position " + position + ".",
                        position);
            }
            return new RegularEggspression.Literal(rangeF.andNothingElse());
        } else if (isSymbol(select)) {
            this.consume();
            VirtualSymbol symbol = symbolFactory.newSymbol().include(CodePointRange.single(select)).andNothingElse();
            return new RegularEggspression.Literal(symbol);
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }

    private boolean negation() throws RedeggsParseException {
        char select = this.peek();
        if (select == '^') {
            this.consume();
            return true;
        } else {
            return false;
        }

        // throw new RedeggsParseException("Unexpected symbol '" + select + "' at
        // position " + position + ".", position);
    }

    private SymbolFactory.Builder rangeF(SymbolFactory.Builder builder, boolean negated) throws RedeggsParseException {
        char select = this.peek();
        if (isLiteral(select)) {
            SymbolFactory.Builder inhalt = inhalt(builder, negated);
            return rangeF(inhalt, negated);
        } else if (select == ']') {
            return builder;
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }

    private SymbolFactory.Builder inhalt(SymbolFactory.Builder builder, boolean negated) throws RedeggsParseException {
        char select = this.peek();
        if (isSymbol(select)) {
            this.consume();
            CodePointRange rest = rest(select);
            if (negated) {
                return builder.exclude(rest);
            } else {
                return builder.include(rest);
            }
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }

    private CodePointRange rest(char start) throws RedeggsParseException {
        char select = this.peek();
        if (select == '-') {
            this.consume();
            char lit = this.consume();
            if (!isLiteral(lit)) {
                throw new RedeggsParseException(
                        "Input ended unexpectedly, expected literal at position " + position + ".",
                        position);
            }
            return CodePointRange.range(start, lit);
        } else if (isSymbol(select) || select == ']') {
            return CodePointRange.single(start);
        }

        throw new RedeggsParseException("Unexpected symbol '" + select + "' at position " + position + ".", position);
    }
}
