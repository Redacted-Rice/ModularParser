package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class DefaultCharLiteralParserTests {

    @Test
    void constructorTest() {
        DefaultCharLiteralParser testee = new DefaultCharLiteralParser();
        assertEquals("DefaultCharParser", testee.getName());
    }

    @Test
    void tryParseLiteralTest() {
        DefaultCharLiteralParser testee = new DefaultCharLiteralParser();

        assertEquals(Optional.empty(), testee.tryParseLiteral(null));
        assertEquals(Optional.empty(), testee.tryParseLiteral("  "));
        assertEquals(Optional.empty(), testee.tryParseLiteral("not quoted"));
        assertEquals(Optional.empty(), testee.tryParseLiteral("A"));

        assertEquals(Optional.of("true"), testee.tryParseLiteral("\"true\""));
        assertEquals(Optional.of("string with num: 42"),
                testee.tryParseLiteral("\"string with num: 42\""));

        assertEquals(Optional.of('A'), testee.tryParseLiteral("'A'"));
        assertEquals(Optional.of('4'), testee.tryParseLiteral("'4'"));
        assertEquals(Optional.of('"'), testee.tryParseLiteral("'\"'"));
        assertEquals(Optional.of('\''), testee.tryParseLiteral("'\''"));

        assertEquals(Optional.empty(), testee.tryParseLiteral("'A4'"));
        assertEquals(Optional.empty(), testee.tryParseLiteral("''"));

        assertEquals(Optional.empty(), testee.tryParseLiteral("\"test mismatched'"));
        assertEquals(Optional.empty(), testee.tryParseLiteral("'test mismatched 2\""));
        assertEquals(Optional.empty(), testee.tryParseLiteral("\"test mismatched 3"));
        assertEquals(Optional.empty(), testee.tryParseLiteral("test mismatched 4\""));
        assertEquals(Optional.empty(), testee.tryParseLiteral("'T"));
        assertEquals(Optional.empty(), testee.tryParseLiteral("F'"));
    }
}
