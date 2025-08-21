package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.literal.DefaultNumberLiteralParser.PrimitiveType;

public class DefaultNumberLiteralParserTests {

    @Test
    void constructorTest() {
        DefaultNumberLiteralParser testee = new DefaultNumberLiteralParser();
        assertEquals("DefaultNumberParser", testee.getName());
    }

    @Test
    void tryParseLiteralTest() {
        DefaultNumberLiteralParser testee = spy(new DefaultNumberLiteralParser());

        doReturn(Optional.of(4)).when(testee).parseWithType(any(), eq(PrimitiveType.UNSPECIFIED));
        doReturn(Optional.of(42)).when(testee).parseWithType(any(), eq(PrimitiveType.INT));
        doReturn(Optional.of(42L)).when(testee).parseWithType(any(), eq(PrimitiveType.LONG));
        doReturn(Optional.of(42.42)).when(testee).parseWithType(any(), eq(PrimitiveType.DOUBLE));

        assertEquals(Optional.empty(), testee.tryParseLiteral(null));
        assertEquals(Optional.empty(), testee.tryParseLiteral("  "));
        assertEquals(Optional.empty(), testee.tryParseLiteral("something"));
        assertEquals(Optional.empty(), testee.tryParseLiteral("a"));

        assertEquals(Optional.of(4), testee.tryParseLiteral("4"));
        assertEquals(Optional.of(4), testee.tryParseLiteral("4E4"));
        assertEquals(Optional.of(42), testee.tryParseLiteral("42i"));
        assertEquals(Optional.of(42), testee.tryParseLiteral("42I"));
        assertEquals(Optional.of(42l), testee.tryParseLiteral("42l"));
        assertEquals(Optional.of(42l), testee.tryParseLiteral("42L"));
        assertEquals(Optional.of(42.42), testee.tryParseLiteral("42.42d"));
        assertEquals(Optional.of(42.42), testee.tryParseLiteral("42.42D"));
    }

    @Test
    void parseWithTypeTest() {
        DefaultNumberLiteralParser testee = spy(new DefaultNumberLiteralParser());

        doReturn(Optional.of(4)).when(testee).parseAnyNonE(any(), eq(PrimitiveType.UNSPECIFIED));
        doReturn(Optional.of(42)).when(testee).parseAnyNonE(any(), eq(PrimitiveType.INT));
        doReturn(Optional.of(42L)).when(testee).parseAnyNonE(any(), eq(PrimitiveType.LONG));
        doReturn(Optional.of(42.42)).when(testee).parseAnyNonE(any(), eq(PrimitiveType.DOUBLE));

        // No e goes to parse any non e
        assertEquals(Optional.of(4), testee.parseWithType("4", PrimitiveType.UNSPECIFIED));
        assertEquals(Optional.of(42), testee.parseWithType("42", PrimitiveType.INT));
        assertEquals(Optional.of(42l), testee.parseWithType("42", PrimitiveType.LONG));
        assertEquals(Optional.of(42.42), testee.parseWithType("42.42", PrimitiveType.DOUBLE));

        assertEquals(Optional.of(424.2), testee.parseWithType("4.242E2", PrimitiveType.DOUBLE));
        assertEquals(Optional.of(424.2), testee.parseWithType("4.242e2", PrimitiveType.DOUBLE));
        assertEquals(Optional.of(42000.0), testee.parseWithType("42E3", PrimitiveType.DOUBLE));
        assertEquals(Optional.of(424.2),
                testee.parseWithType("4.242E2", PrimitiveType.UNSPECIFIED));
        assertEquals(Optional.of(4.242),
                testee.parseWithType("4242E-3", PrimitiveType.UNSPECIFIED));

        assertEquals(Optional.of(42000), testee.parseWithType("42E3", PrimitiveType.INT));
        assertEquals(Optional.of(42000), testee.parseWithType("42E3", PrimitiveType.UNSPECIFIED));

        assertEquals(Optional.of(42000L), testee.parseWithType("42E3", PrimitiveType.LONG));
        assertEquals(Optional.of(42000000000L),
                testee.parseWithType("42E9", PrimitiveType.UNSPECIFIED));

        // bad value with E and bad type
        assertEquals(Optional.empty(), testee.parseWithType("4.2E-1", PrimitiveType.INT));
        assertEquals(Optional.empty(), testee.parseWithType("42E9", PrimitiveType.INT));
        assertEquals(Optional.empty(), testee.parseWithType("4.2E-1", PrimitiveType.LONG));
        assertEquals(Optional.empty(), testee.parseWithType("4E4.2", PrimitiveType.UNSPECIFIED));
    }

    @Test
    void parseAnyNonETest() {
        DefaultNumberLiteralParser testee = new DefaultNumberLiteralParser();

        assertEquals(Optional.of(4), testee.parseAnyNonE("4", PrimitiveType.INT));
        assertEquals(Optional.empty(), testee.parseAnyNonE("A", PrimitiveType.INT));
        assertEquals(Optional.empty(), testee.parseAnyNonE("word", PrimitiveType.INT));
        assertEquals(Optional.of(4L), testee.parseAnyNonE("4", PrimitiveType.LONG));
        assertEquals(Optional.of(42000000000L),
                testee.parseAnyNonE("42000000000", PrimitiveType.LONG));
        assertEquals(Optional.empty(), testee.parseAnyNonE("A", PrimitiveType.LONG));
        assertEquals(Optional.empty(), testee.parseAnyNonE("word", PrimitiveType.LONG));
        assertEquals(Optional.of(4.), testee.parseAnyNonE("4", PrimitiveType.DOUBLE));
        assertEquals(Optional.of(4.2), testee.parseAnyNonE("4.2", PrimitiveType.DOUBLE));
        assertEquals(Optional.empty(), testee.parseAnyNonE("A", PrimitiveType.DOUBLE));
        assertEquals(Optional.empty(), testee.parseAnyNonE("word", PrimitiveType.DOUBLE));

        assertEquals(Optional.of(4), testee.parseAnyNonE("4", PrimitiveType.UNSPECIFIED));
        assertEquals(Optional.of(42000000000L),
                testee.parseAnyNonE("42000000000", PrimitiveType.UNSPECIFIED));
        assertEquals(Optional.of(4.2), testee.parseAnyNonE("4.2", PrimitiveType.UNSPECIFIED));
        assertEquals(Optional.empty(), testee.parseAnyNonE("A", PrimitiveType.UNSPECIFIED));
        assertEquals(Optional.empty(), testee.parseAnyNonE("word", PrimitiveType.UNSPECIFIED));
    }
}
