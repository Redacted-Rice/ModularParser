package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserMultiTypeTests {

    private class TestMultiType extends ArgParserMultiType {

        protected TestMultiType(boolean allowNull) {
            super(allowNull);
        }

        @Override
        protected Response<Object> tryParseNonNullArgument(Response<Object> parsed,
                String argument) {
            return argument.equals("unhandled") ? Response.notHandled() : Response.is(argument);
        }

        @Override
        protected Collection<Class<?>> expectedTypes() {
            return List.of(Integer.class, String.class);
        }
    }

    @Test
    void constuctor() {
        ArgParserMultiType testee = new TestMultiType(true);
        assertEquals(true, testee.allowNull);

        testee = new TestMultiType(false);
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument_type() {
        ArgParserMultiType testee = spy(new TestMultiType(false));
        Response<Object> unhandled = Response.notHandled();

        assertTrue(testee.tryParseArgument(Response.is(5), "doesn't matter").wasValueReturned());
        assertTrue(testee.tryParseArgument(unhandled, "doesn't matter").wasValueReturned());

        assertFalse(testee.tryParseArgument(unhandled, "unhandled").wasValueReturned());
    }

    @Test
    void tryParseArgument_nullable() {
        ArgParserMultiType testee = spy(new TestMultiType(true));
        Response<Object> unhandled = Response.notHandled();

        assertTrue(testee.tryParseArgument(Response.is(null), "something").wasValueReturned());
        assertTrue(testee.tryParseArgument(unhandled, "\t").wasValueReturned());
        assertTrue(testee.tryParseArgument(unhandled, "null").wasValueReturned());
    }

    @Test
    void tryParseArgument_nonNullable() {
        ArgParserMultiType testee = spy(new TestMultiType(false));
        Response<Object> unhandled = Response.notHandled();

        assertFalse(testee.tryParseArgument(Response.is(null), "something").wasValueReturned());
        assertFalse(testee.tryParseArgument(unhandled, "\t").wasValueReturned());
        assertFalse(testee.tryParseArgument(unhandled, "null").wasValueReturned());
    }

    @Test
    void modifyMatchingResponse() {
        ArgParserMultiType testee = spy(new TestMultiType(false));
        assertEquals("test", testee.modifyMatchingResponse(Response.is("test")).getValue());
        assertEquals(6, testee.modifyMatchingResponse(Response.is(6)).getValue());
    }
}
