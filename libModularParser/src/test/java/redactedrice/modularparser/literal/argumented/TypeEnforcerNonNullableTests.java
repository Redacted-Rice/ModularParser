package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeEnforcerNonNullableTests {

    private TypeEnforcerNonNullable<Integer> testee;

    @BeforeEach
    void setup() {
        testee = new TypeEnforcerNonNullable<>(Integer.class);
    }

    @Test
    void constuctor() {
        assertEquals(Integer.class, testee.clazz);
    }

    @Test
    void parseArgument() {
        // No parsing supported
        assertTrue(testee.parseArgument("5").wasNotHandled());
        // null not allowed
        assertTrue(testee.parseArgument(null).wasNotHandled());
        assertTrue(testee.parseArgument("\t").wasNotHandled());
        assertTrue(testee.parseArgument("null").wasNotHandled());
    }

    @Test
    void isExpectedType() {
        assertTrue(testee.isExpectedType(5));

        // Null not allowed
        assertFalse(testee.isExpectedType(null));
        assertFalse(testee.isExpectedType("5"));
        assertFalse(testee.isExpectedType(4.2));
    }

    @Test
    void getExpectedTypeName() {
        assertEquals(Integer.class.getSimpleName(), testee.getExpectedTypeName());
    }
}
