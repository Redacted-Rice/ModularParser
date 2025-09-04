package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.DefaultNumberLiteralParser.PrimitiveType;

public class DefaultNumberLiteralParserTests {

    @Test
    void constructorTest() {
        DefaultNumberLiteralParser testee = new DefaultNumberLiteralParser();
        assertEquals(DefaultNumberLiteralParser.class.getSimpleName(), testee.getName());
    }

    @Test
    void tryParseLiteralTest() {
        DefaultNumberLiteralParser testee = spy(new DefaultNumberLiteralParser());

        doReturn(Response.is(4)).when(testee).parseWithType(any(), eq(PrimitiveType.UNSPECIFIED));
        doReturn(Response.is(42)).when(testee).parseWithType(any(), eq(PrimitiveType.INT));
        doReturn(Response.is(42L)).when(testee).parseWithType(any(), eq(PrimitiveType.LONG));
        doReturn(Response.is(42.42)).when(testee).parseWithType(any(), eq(PrimitiveType.DOUBLE));

        assertEquals(Response.notHandled(), testee.tryParseLiteral("something"));
        assertEquals(Response.notHandled(), testee.tryParseLiteral("a"));

        assertEquals(Response.is(4), testee.tryParseLiteral("4"));
        assertEquals(Response.is(4), testee.tryParseLiteral("4E4"));
        assertEquals(Response.is(42), testee.tryParseLiteral("42i"));
        assertEquals(Response.is(42), testee.tryParseLiteral("42I"));
        assertEquals(Response.is(42l), testee.tryParseLiteral("42l"));
        assertEquals(Response.is(42l), testee.tryParseLiteral("42L"));
        assertEquals(Response.is(42.42), testee.tryParseLiteral("42.42d"));
        assertEquals(Response.is(42.42), testee.tryParseLiteral("42.42D"));
    }

    @Test
    void parseWithTypeTest() {
        DefaultNumberLiteralParser testee = spy(new DefaultNumberLiteralParser());

        doReturn(Response.is(4)).when(testee).parseAnyNonE(any(), eq(PrimitiveType.UNSPECIFIED));
        doReturn(Response.is(42)).when(testee).parseAnyNonE(any(), eq(PrimitiveType.INT));
        doReturn(Response.is(42L)).when(testee).parseAnyNonE(any(), eq(PrimitiveType.LONG));
        doReturn(Response.is(42.42)).when(testee).parseAnyNonE(any(), eq(PrimitiveType.DOUBLE));

        // No e goes to parse any non e
        assertEquals(Response.is(4), testee.parseWithType("4", PrimitiveType.UNSPECIFIED));
        assertEquals(Response.is(42), testee.parseWithType("42", PrimitiveType.INT));
        assertEquals(Response.is(42l), testee.parseWithType("42", PrimitiveType.LONG));
        assertEquals(Response.is(42.42), testee.parseWithType("42.42", PrimitiveType.DOUBLE));

        assertEquals(Response.is(424.2), testee.parseWithType("4.242E2", PrimitiveType.DOUBLE));
        assertEquals(Response.is(424.2), testee.parseWithType("4.242e2", PrimitiveType.DOUBLE));
        assertEquals(Response.is(42000.0), testee.parseWithType("42E3", PrimitiveType.DOUBLE));
        assertEquals(Response.is(424.2),
                testee.parseWithType("4.242E2", PrimitiveType.UNSPECIFIED));
        assertEquals(Response.is(4.242),
                testee.parseWithType("4242E-3", PrimitiveType.UNSPECIFIED));

        assertEquals(Response.is(42000), testee.parseWithType("42E3", PrimitiveType.INT));
        assertEquals(Response.is(42000), testee.parseWithType("42E3", PrimitiveType.UNSPECIFIED));

        assertEquals(Response.is(42000L), testee.parseWithType("42E3", PrimitiveType.LONG));
        assertEquals(Response.is(42000000000L),
                testee.parseWithType("42E9", PrimitiveType.UNSPECIFIED));

        // bad value with E and bad type
        assertTrue(testee.parseWithType("4.2E-1", PrimitiveType.INT).wasError());
        assertTrue(testee.parseWithType("42E9", PrimitiveType.INT).wasError());
        assertTrue(testee.parseWithType("4.2E-1", PrimitiveType.LONG).wasError());
        assertEquals(Response.notHandled(),
                testee.parseWithType("4E4.2", PrimitiveType.UNSPECIFIED));
        assertEquals(Response.notHandled(),
                testee.parseWithType("Some String", PrimitiveType.UNSPECIFIED));
    }

    @Test
    void parseAnyNonETest() {
        DefaultNumberLiteralParser testee = new DefaultNumberLiteralParser();

        assertEquals(Response.is(4), testee.parseAnyNonE("4", PrimitiveType.INT));
        assertEquals(Response.notHandled(), testee.parseAnyNonE("A", PrimitiveType.INT));
        assertEquals(Response.notHandled(), testee.parseAnyNonE("word", PrimitiveType.INT));
        assertEquals(Response.is(4L), testee.parseAnyNonE("4", PrimitiveType.LONG));
        assertEquals(Response.is(42000000000L),
                testee.parseAnyNonE("42000000000", PrimitiveType.LONG));
        assertEquals(Response.notHandled(), testee.parseAnyNonE("A", PrimitiveType.LONG));
        assertEquals(Response.notHandled(), testee.parseAnyNonE("word", PrimitiveType.LONG));
        assertEquals(Response.is(4.), testee.parseAnyNonE("4", PrimitiveType.DOUBLE));
        assertEquals(Response.is(4.2), testee.parseAnyNonE("4.2", PrimitiveType.DOUBLE));
        assertEquals(Response.notHandled(), testee.parseAnyNonE("A", PrimitiveType.DOUBLE));
        assertEquals(Response.notHandled(), testee.parseAnyNonE("word", PrimitiveType.DOUBLE));

        assertEquals(Response.is(4), testee.parseAnyNonE("4", PrimitiveType.UNSPECIFIED));
        assertEquals(Response.is(42000000000L),
                testee.parseAnyNonE("42000000000", PrimitiveType.UNSPECIFIED));
        assertEquals(Response.is(4.2), testee.parseAnyNonE("4.2", PrimitiveType.UNSPECIFIED));
        assertEquals(Response.notHandled(), testee.parseAnyNonE("A", PrimitiveType.UNSPECIFIED));
        assertEquals(Response.notHandled(), testee.parseAnyNonE("word", PrimitiveType.UNSPECIFIED));
    }
}
