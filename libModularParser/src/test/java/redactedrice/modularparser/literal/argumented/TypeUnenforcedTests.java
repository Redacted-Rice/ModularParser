package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeUnenforcedTests {

    private TypeUnenforced testee;

    @BeforeEach
    void setup() {
        testee = new TypeUnenforced();
    }

    @Test
    void parseArgument() {
        // No parsing supported
        assertTrue(testee.parseArgument("5").wasNotHandled());
        assertTrue(testee.parseArgument(null).wasNotHandled());
        assertTrue(testee.parseArgument("\t").wasNotHandled());
        assertTrue(testee.parseArgument("null").wasNotHandled());
    }

    @Test
    void isExpectedType() {
        // always allowed, even null
        assertTrue(testee.isExpectedType(5));
        assertTrue(testee.isExpectedType(null));
        assertTrue(testee.isExpectedType("5"));
        assertTrue(testee.isExpectedType(4.2));
    }

    @Test
    void getExpectedTypeName() {
        assertEquals(Object.class.getSimpleName(), testee.getExpectedTypeName());
    }
}
