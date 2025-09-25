package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeEnforcerTests {

    private TypeEnforcer<Integer> testee;

    @BeforeEach
    void setup() {
        testee = new TypeEnforcer<>(Integer.class);
    }

    @Test
    void constuctor() {
        assertEquals(Integer.class, testee.clazz);
    }

    @Test
    void parseArgument() {
        // No parsing supported. Will only handle null cases
        assertTrue(testee.parseArgument("5").wasNotHandled());

        assertTrue(testee.parseArgument(null).wasHandled());
        assertTrue(testee.parseArgument("\t").wasHandled());
        assertTrue(testee.parseArgument("null").wasHandled());
    }

    @Test
    void isExpectedType() {
        assertTrue(testee.isExpectedType(5));
        // Null is allowed for this enforcer
        assertTrue(testee.isExpectedType(null));

        assertFalse(testee.isExpectedType("5"));
        assertFalse(testee.isExpectedType(4.2));
    }

    @Test
    void getExpectedTypeName() {
        assertEquals(Integer.class.getSimpleName(), testee.getExpectedTypeName());
    }
}
