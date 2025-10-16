package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgumentUtilsTests {

    @Test
    void testUnquoteStringVariants() {
        assertNull(ArgumentUtils.unquoteString(null));
        assertEquals("", ArgumentUtils.unquoteString(""));
        assertEquals("a", ArgumentUtils.unquoteString("a"));
        assertEquals("\"a", ArgumentUtils.unquoteString("\"a"));
        assertEquals("a\"", ArgumentUtils.unquoteString("a\""));
        assertEquals("abc", ArgumentUtils.unquoteString("\"abc\""));
        assertEquals("abc\"", ArgumentUtils.unquoteString("abc\""));
        assertEquals("\"abc", ArgumentUtils.unquoteString("\"abc"));
        assertEquals("abc", ArgumentUtils.unquoteString("abc"));
    }

    @Test
    void testGetUnquotedStringVariants() {
        assertEquals("quoted",
                ArgumentUtils.getUnquotedString(Response.is("\"quoted\""), "fallback"));
        assertEquals("plain", ArgumentUtils.getUnquotedString(Response.is("plain"), "fallback"));
        assertEquals("fallback", ArgumentUtils.getUnquotedString(Response.is(123), "fallback"));
        assertEquals("fallback",
                ArgumentUtils.getUnquotedString(Response.notHandled(), "fallback"));
        assertEquals("fallback", ArgumentUtils.getUnquotedString(null, "fallback"));
    }
}
