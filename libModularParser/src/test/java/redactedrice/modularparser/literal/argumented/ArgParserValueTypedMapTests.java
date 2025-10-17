package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserValueTypedMapTests {

    private class TestValueTypedMap extends ArgParserValueTypedMapBase {

        protected TestValueTypedMap(boolean allowNull) {
            super(allowNull);
        }

        @Override
        protected Class<?> expectedType() {
            return Integer.class;
        }
    }

    @Test
    void constuctor() {
        ArgParserValueTypedMapBase testee = new TestValueTypedMap(true);
        assertEquals(true, testee.allowNull);

        testee = new TestValueTypedMap(false);
        assertEquals(false, testee.allowNull);
    }

    @Test
    void preparseEvaluate() {
        ArgParserValueTypedMapBase testee = new TestValueTypedMap(true);
        assertTrue(testee.preparseEvaluate("anything").wasNotHandled());
    }

    @SuppressWarnings("unchecked")
    @Test
    void tryParseArgument_type() {
        ArgParserValueTypedMapBase testee = new TestValueTypedMap(false);
        Response<Object> unhandled = Response.notHandled();

        Map<Object, Object> rightMap = Map.of("one", 1, "two", 2);
        Map<Object, Object> wrongMap = Map.of("one", 1L, "two", 2L);
        Map<Object, Object> mixedMap = Map.of("one", 1L, "two", 2.0, "three", 3);
        assertIterableEquals(rightMap.entrySet(),
                ((Map<Object, Object>) testee
                        .tryParseArgument(Response.is(rightMap), "doesn't matter").getValue())
                        .entrySet());

        assertFalse(testee.tryParseArgument(Response.is(wrongMap), "doesn't matter")
                .wasValueReturned());
        assertFalse(testee.tryParseArgument(Response.is(mixedMap), "doesn't matter")
                .wasValueReturned());
        assertFalse(testee.tryParseArgument(Response.is(5), "doesn't matter").wasValueReturned());
        assertFalse(testee.tryParseArgument(unhandled, "doesn't matter").wasValueReturned());
    }

    @Test
    void tryParseArgument_nullable() {
        ArgParserValueTypedMapBase testee = new TestValueTypedMap(true);
        Response<Object> unhandled = Response.notHandled();

        assertTrue(testee.tryParseArgument(Response.is(null), "something").wasValueReturned());
        assertTrue(testee.tryParseArgument(unhandled, "\t").wasValueReturned());
        assertTrue(testee.tryParseArgument(unhandled, "null").wasValueReturned());
    }

    @Test
    void tryParseArgument_nonNullable() {
        ArgParserValueTypedMapBase testee = new TestValueTypedMap(false);
        Response<Object> unhandled = Response.notHandled();

        assertFalse(testee.tryParseArgument(Response.is(null), "something").wasValueReturned());
        assertFalse(testee.tryParseArgument(unhandled, "\t").wasValueReturned());
        assertFalse(testee.tryParseArgument(unhandled, "null").wasValueReturned());
    }

    @Test
    void expectedType() {
        ArgParserValueTypedMapBase testee = new TestValueTypedMap(true);
        assertEquals(Integer.class, testee.expectedType());
    }
}
