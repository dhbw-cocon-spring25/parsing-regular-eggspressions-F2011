package de.dhbw.mh.redeggs;

import static de.dhbw.mh.redeggs.CodePointRange.range;
import static de.dhbw.mh.redeggs.CodePointRange.single;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.dhbw.mh.redeggs.RegularEggspression.Alternation;
import de.dhbw.mh.redeggs.RegularEggspression.Concatenation;
import de.dhbw.mh.redeggs.RegularEggspression.EmptySet;
import de.dhbw.mh.redeggs.RegularEggspression.EmptyWord;
import de.dhbw.mh.redeggs.RegularEggspression.Literal;
import de.dhbw.mh.redeggs.RegularEggspression.Star;

/**
 * Unit tests for the {@link RecursiveDescentRedeggsParserTest}, using a custom
 * testable {@link VirtualSymbol} and a simplified {@link SymbolFactory}.
 *
 * <p>
 * This test class verifies the correct parsing and AST generation of various
 * regular expression constructs such as literals, concatenation, alternation,
 * repetition, the empty word, and the empty set.
 * </p>
 */
public class RecursiveDescentRedeggsParserTest {

    /**
     * A simple implementation of {@link VirtualSymbol} for testing. Holds a sorted
     * list of character ranges that represent the symbol.
     */
    public static class TestableSymbol implements VirtualSymbol {

        /** List of character ranges representing the symbol. */
        public final List<CodePointRange> ranges = new LinkedList<>();

        /** Comparator for sorting ranges by their starting codepoint. */
        private static final Comparator<CodePointRange> BY_FIRST_CODEPOINT = Comparator
                .comparingInt(r -> r.firstCodePoint);

        /**
         * Constructs a new {@code TestableSymbol} with the given ranges.
         *
         * @param ranges character ranges to include in the symbol
         */
        public TestableSymbol(List<CodePointRange> ranges) {
            super();
            this.ranges.addAll(ranges);
            this.ranges.sort(BY_FIRST_CODEPOINT);
        }

        @Override
        public String toString() {
            return ranges.stream().map(Object::toString).collect(Collectors.joining("", "[", "]"));
        }

        @Override
        public List<CodePointRange> sortedCodePointRanges() {
            return null;
        }
    }

    /**
     * A test-specific implementation of {@link SymbolFactory} that builds
     * {@link TestableSymbol} instances.
     */
    private final static SymbolFactory SYMBOL_FACTORY = new SymbolFactory() {

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
                    return new TestableSymbol(ranges);
                }

            };
        }

    };

    /** The parser under test. */
    private final RecursiveDescentRedeggsParser parser = new RecursiveDescentRedeggsParser(SYMBOL_FACTORY);

    /** A node visitor used to inspect the regular expression tree. */
    public static final NodeInspector INSPECTOR = new NodeInspector();

    @Test
    public void testCharacterClass() throws Exception {
        RegularEggspression expr = parser.parse("[_a-zA-Z]");

        assertThat(expr).isInstanceOf(Literal.class);
        assertThat(expr.accept(INSPECTOR)).isEqualTo(SYMBOL_FACTORY.newSymbol()
                .include(range('A', 'Z'), single('_'), range('a', 'z')).andNothingElse().toString());
    }

    @Test
    public void testLiteral() throws Exception {
        RegularEggspression expr = parser.parse("a");

        assertThat(expr).isInstanceOf(Literal.class);
        assertThat(expr.accept(INSPECTOR)).isEqualTo("[\\u0061]");
    }

    @Test
    public void testConcatenation() throws Exception {
        RegularEggspression expr = parser.parse("ab");

        assertThat(expr).isInstanceOf(Concatenation.class);
        assertThat(expr.accept(INSPECTOR)).isEqualTo("([\\u0061][\\u0062])");
    }

    @Test
    public void testAlternation() throws Exception {
        RegularEggspression expr = parser.parse("a|b");

        assertThat(expr).isInstanceOf(Alternation.class);
        assertThat(expr.accept(INSPECTOR)).isEqualTo("([\\u0061]|[\\u0062])");
    }

    @Test
    public void testStar() throws Exception {
        RegularEggspression expr = parser.parse("a*");

        assertThat(expr).isInstanceOf(Star.class);
        assertThat(expr.accept(INSPECTOR)).isEqualTo("([\\u0061])*");
    }

    @Test
    public void testEmptyWord() throws Exception {
        RegularEggspression expr = parser.parse("ε");

        assertThat(expr).isInstanceOf(EmptyWord.class);
        assertThat(expr.accept(INSPECTOR)).isEqualTo("ε");
    }

    @Test
    public void testEmptySet() throws Exception {
        RegularEggspression expr = parser.parse("∅");

        assertThat(expr).isInstanceOf(EmptySet.class);
        assertThat(expr.accept(INSPECTOR)).isEqualTo("∅");
    }

    @Test
    public void testParentheses() throws Exception {
        RegularEggspression expr = parser.parse("a(b|c)");

        assertThat(expr).isInstanceOf(Concatenation.class);
        assertThat(expr.accept(INSPECTOR)).isEqualTo("([\\u0061]([\\u0062]|[\\u0063]))");
    }

    @Test
    public void randomTest() throws RedeggsParseException {
        RegularEggspression expr = parser.parse("([0-9a-fA-F]|[Yyz+])*");

        assertThat(expr).isInstanceOf(Star.class);
        assertThat(expr.accept(INSPECTOR)).isEqualTo(
                String.format(
                        "((%s|%s))*",
                        SYMBOL_FACTORY.newSymbol()
                                .include(range('0', '9'), range('A', 'F'), range('a', 'f')).andNothingElse()
                                .toString(),
                        SYMBOL_FACTORY.newSymbol().include(single('Y'), single('y'), single('z'), single('+'))
                                .andNothingElse().toString()));
    }

    public static final Random RANDOM = new Random();

    private static final Set<Integer> EXCLUDED_CODEPOINTS = Set.of((int) '*', (int) '+', (int) '.', (int) '|',
            (int) '(', (int) ')', (int) '[', (int) ']', (int) 'ε', (int) '∅');

    public static Integer[] getRandomCodepoints(int number) {
        int maxCodePoint = 0xffff;
        Integer[] result = new Integer[number];
        int count = 0;

        while (count < number) {
            int cp = 33 + RANDOM.nextInt(maxCodePoint - 33 + 1);

            if (!EXCLUDED_CODEPOINTS.contains(cp) && Character.isDefined(cp)) {
                result[count++] = cp;
            }
        }

        return result;
    }

    public static String join(int[] array, String separator) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length - 1; i++) {
            stringBuilder.appendCodePoint(array[i]);
            stringBuilder.append(separator);
        }
        stringBuilder.appendCodePoint(array[array.length - 1]);
        return stringBuilder.toString();
    }

    @Test
    @Tag("associativity")
    public void concatenationIsLeftAssociative() throws Exception {
        Object[] randomChars = getRandomCodepoints(3);
        String input = String.format("%c%c%c", randomChars);

        RegularEggspression expr = parser.parse(input);

        String expectation = String.format("(([\\u%04X][\\u%04X])[\\u%04X])", randomChars);
        String result = expr.accept(INSPECTOR);
        if (!result.equals(expectation)) {
            fail("Concatenation is not left-associative.");
        }
    }

    @Test
    @Tag("associativity")
    public void alternationIsLeftAssociative() throws Exception {
        Object[] randomChars = getRandomCodepoints(3);
        String input = String.format("%c|%c|%c", randomChars);

        RegularEggspression expr = parser.parse(input);

        String expectation = String.format("(([\\u%04X]|[\\u%04X])|[\\u%04X])", randomChars);
        String result = expr.accept(INSPECTOR);
        if (!result.equals(expectation)) {
            fail("Alternation is not left-associative.");
        }
    }

    @Test
    @Tag("precedence")
    public void concatenationTakesPrecedence() throws RedeggsParseException {
        Object[] randomChars = getRandomCodepoints(3);
        String input = String.format("%c|%c%c", randomChars);

        RegularEggspression expr = parser.parse(input);

        String expectation = String.format("([\\u%04X]|([\\u%04X][\\u%04X]))", randomChars);
        String result = expr.accept(INSPECTOR);
        if (!result.equals(expectation)) {
            fail("Concatenation yields precedence to alternation.");
        }
    }

    @Test
    @Tag("precedence")
    public void kleeneStarTakesPrecedence() throws RedeggsParseException {
        Object[] randomChars = getRandomCodepoints(2);
        String input = String.format("%c%c*", randomChars);

        RegularEggspression expr = parser.parse(input);

        String expectation = String.format("([\\u%04X]([\\u%04X])*)", randomChars);
        String result = expr.accept(INSPECTOR);
        if (!result.equals(expectation)) {
            fail("Concatenation yields precedence to alternation.");
        }
    }

    @Test
    @Tag("character-classes")
    public void charClasAcceptsMultipleCodepoints() throws RedeggsParseException {
        Object[] randomChars = getRandomCodepoints(3);
        String input = String.format("[%c%c%c]", randomChars);

        RegularEggspression expr = parser.parse(input);

        Arrays.sort(randomChars);
        String expectation = String.format("[\\u%04X\\u%04X\\u%04X]", randomChars);
        String result = expr.accept(INSPECTOR);
        if (!result.equals(expectation)) {
            fail("Concatenation yields precedence to alternation.");
        }
    }

    @Test
    @Tag("character-classes")
    public void leftBracketCannotBeNested() throws RedeggsParseException {
        String input = "[[]";

        RegularEggspression expr = parser.parse(input);

        String expectation = String.format("[\\u%04X]", (int) '[');
        String result = expr.accept(INSPECTOR);
        if (!result.equals(expectation)) {
            fail("Character classes cannot be nested.");
        }
    }

    @Test
    @Tag("edge-cases")
    public void dashHasNoFunctionOutsideCharClass() throws RedeggsParseException {
        String input = "a-z";

        RegularEggspression expr = parser.parse(input);

        String expectation = String.format("(([\\u%04X][\\u%04X])[\\u%04X])", (int) 'a', (int) '-', (int) 'z');
        String result = expr.accept(INSPECTOR);
        if (!result.equals(expectation)) {
            fail("Intervals do not exist outside of character classes.");
        }
    }

    @Test
    @Tag("parentheses")
    public void doublyClosedCharClassThrowsException() throws RedeggsParseException {
        String input = "[[]]";

        assertThatExceptionOfType(RedeggsParseException.class)
                .as("Doubly closed character class should raise a RedeggsParseException.")
                .isThrownBy(() -> parser.parse(input)).withMessage("Unexpected symbol '%c' at position %d.", ']', 4);
    }

    @Test
    @Tag("parentheses")
    public void missingRightParenThrowsException() throws RedeggsParseException {
        Object[] randomChars = getRandomCodepoints(1);
        String input = String.format("((%c)", randomChars);

        assertThatExceptionOfType(RedeggsParseException.class)
                .as("Unbalanced parentheses should raise a RedeggsParseException.")
                .isThrownBy(() -> parser.parse(input))
                .withMessage("Input ended unexpectedly, expected symbol '%c' at position %d.", ')', 5);
    }

    @Test
    @Tag("parentheses")
    public void unbalancedRightParenThrowsException() throws RedeggsParseException {
        Object[] randomChars = getRandomCodepoints(1);
        String input = String.format("((%c)))", randomChars);

        assertThatExceptionOfType(RedeggsParseException.class)
                .as("Unbalanced parentheses should raise a RedeggsParseException.")
                .isThrownBy(() -> parser.parse(input)).withMessage("Unexpected symbol '%c' at position %d.", ')', 6);
    }

}