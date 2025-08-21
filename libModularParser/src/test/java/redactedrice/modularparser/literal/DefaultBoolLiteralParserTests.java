package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class DefaultBoolLiteralParserTests {

    @Test
    void constructorTest() {
        DefaultBoolLiteralParser testee = new DefaultBoolLiteralParser();
        assertEquals("DefaultBoolParser", testee.getName());
    }

    @Test
    void tryParseLiteralTest() {
        DefaultBoolLiteralParser testee = new DefaultBoolLiteralParser();

        assertEquals(Optional.empty(), testee.tryParseLiteral(null));
        assertEquals(Optional.empty(), testee.tryParseLiteral(""));
        assertEquals(Optional.empty(), testee.tryParseLiteral("something"));
        assertEquals(Optional.empty(), testee.tryParseLiteral("fal"));

        assertEquals(Optional.of(false), testee.tryParseLiteral("f"));
        assertEquals(Optional.of(false), testee.tryParseLiteral("F"));
        assertEquals(Optional.of(false), testee.tryParseLiteral("false"));
        assertEquals(Optional.of(false), testee.tryParseLiteral("FaLSe"));

        assertEquals(Optional.of(true), testee.tryParseLiteral("t"));
        assertEquals(Optional.of(true), testee.tryParseLiteral("T"));
        assertEquals(Optional.of(true), testee.tryParseLiteral("true"));
        assertEquals(Optional.of(true), testee.tryParseLiteral("TrUe"));
    }
}
