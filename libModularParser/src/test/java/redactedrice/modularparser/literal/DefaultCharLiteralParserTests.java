package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

public class DefaultCharLiteralParserTests {

    @Test
    void constructorTest() {
        DefaultCharLiteralParser testee = new DefaultCharLiteralParser();
        assertEquals(DefaultCharLiteralParser.class.getSimpleName(), testee.getName());
    }

    @Test
    void tryParseLiteralTest() {
        DefaultCharLiteralParser testee = new DefaultCharLiteralParser();

        assertEquals(Response.notHandled(), testee.tryParseLiteral("not quoted"));
        assertEquals(Response.notHandled(), testee.tryParseLiteral("A"));

        assertEquals(Response.is("true"), testee.tryParseLiteral("\"true\""));
        assertEquals(Response.is("string with num: 42"),
                testee.tryParseLiteral("\"string with num: 42\""));

        assertEquals(Response.is('A'), testee.tryParseLiteral("'A'"));
        assertEquals(Response.is('4'), testee.tryParseLiteral("'4'"));
        assertEquals(Response.is('"'), testee.tryParseLiteral("'\"'"));
        assertEquals(Response.is('\''), testee.tryParseLiteral("'\''"));

        assertEquals(Response.notHandled(), testee.tryParseLiteral("'A4'"));
        assertEquals(Response.notHandled(), testee.tryParseLiteral("''"));

        assertEquals(Response.notHandled(), testee.tryParseLiteral("\"test mismatched'"));
        assertEquals(Response.notHandled(), testee.tryParseLiteral("'test mismatched 2\""));
        assertEquals(Response.notHandled(), testee.tryParseLiteral("\"test mismatched 3"));
        assertEquals(Response.notHandled(), testee.tryParseLiteral("test mismatched 4\""));
        assertEquals(Response.notHandled(), testee.tryParseLiteral("'T"));
        assertEquals(Response.notHandled(), testee.tryParseLiteral("F'"));
    }
}
