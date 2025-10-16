package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserUnquotedStringTests {

    @Test
    void constuctor() {
        ArgParserUnquotedString testee = new ArgParserUnquotedString(true);
        assertEquals(true, testee.allowNull);

        testee = new ArgParserUnquotedString(false);
        assertEquals(false, testee.allowNull);

        testee = new ArgParserUnquotedString();
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument() {
        ArgParserUnquotedString testee = new ArgParserUnquotedString(true);

        assertNull(testee.tryParseArgument(Response.is(null), "42").getValue());
        assertEquals("42", testee.tryParseArgument(Response.notHandled(), "42").getValue());

        assertEquals("42", testee.tryParseArgument(Response.is(5), "42").getValue());
        assertEquals("5", testee.tryParseArgument(Response.is("5"), "42").getValue());
        assertEquals("test", testee.tryParseArgument(Response.is("\"test\""), "42").getValue());
        assertEquals("test", testee.tryParseArgument(Response.is(5), "\"test\"").getValue());
        assertEquals("test", testee.tryParseArgument(Response.is(5), "test").getValue());
    }

    @Test
    void tryParseNonNullArgument() {
        ArgParserUnquotedString testee = new ArgParserUnquotedString(false);

        assertEquals("42", testee.tryParseArgument(Response.notHandled(), "42").getValue());

        assertEquals("42", testee.tryParseNonNullArgument(Response.is(5), "42").getValue());
        assertEquals("5", testee.tryParseNonNullArgument(Response.is("5"), "42").getValue());
        assertEquals("test",
                testee.tryParseNonNullArgument(Response.is("\"test\""), "42").getValue());
        assertEquals("test", testee.tryParseNonNullArgument(Response.is(5), "\"test\"").getValue());
        assertEquals("test", testee.tryParseNonNullArgument(Response.is(5), "test").getValue());
    }

    @Test
    void modifyMatchingResponse() {
        ArgParserUnquotedString testee = new ArgParserUnquotedString();
        String expected = "test";
        assertEquals(expected, testee.modifyMatchingResponse(Response.is("test")).getValue());
        assertEquals(expected, testee.modifyMatchingResponse(Response.is("\"test\"")).getValue());
        assertEquals("'" + expected + "'",
                testee.modifyMatchingResponse(Response.is("'test'")).getValue());
        assertEquals("", testee.modifyMatchingResponse(Response.is("")).getValue());
        assertEquals("   ", testee.modifyMatchingResponse(Response.is("\"   \"")).getValue());
    }

    @Test
    void expectedType() {
        ArgParserUnquotedString testee = new ArgParserUnquotedString();
        assertEquals(String.class, testee.expectedType());
    }
}
