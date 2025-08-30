package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

public class DefaultBoolLiteralParserTests {

    @Test
    void constructorTest() {
        DefaultBoolLiteralParser testee = new DefaultBoolLiteralParser();
        assertEquals("DefaultBoolParser", testee.getName());
    }

    @Test
    void tryParseLiteralTest() {
        DefaultBoolLiteralParser testee = new DefaultBoolLiteralParser();

        assertEquals(Response.notHandled(), testee.tryParseLiteral("something"));
        assertEquals(Response.notHandled(), testee.tryParseLiteral("fal"));

        assertEquals(Response.is(false), testee.tryParseLiteral("f"));
        assertEquals(Response.is(false), testee.tryParseLiteral("F"));
        assertEquals(Response.is(false), testee.tryParseLiteral("false"));
        assertEquals(Response.is(false), testee.tryParseLiteral("FaLSe"));

        assertEquals(Response.is(true), testee.tryParseLiteral("t"));
        assertEquals(Response.is(true), testee.tryParseLiteral("T"));
        assertEquals(Response.is(true), testee.tryParseLiteral("true"));
        assertEquals(Response.is(true), testee.tryParseLiteral("TrUe"));
    }
}
