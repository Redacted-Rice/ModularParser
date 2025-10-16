package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserStringEnumTests {

    private class TestStringEnum extends ArgParserStringEnum<Integer> {
        protected TestStringEnum(Map<String, Integer> enumMap, boolean allowNull) {
            super(enumMap, allowNull);
        }

        protected TestStringEnum(Map<String, Integer> enumMap) {
            super(enumMap);
        }
    }

    @Test
    void constuctor() {
        String valName = "name";
        int val = 42;
        String valName2 = "name2";
        int val2 = 5;
        Map<String, Integer> values = Map.of(valName, val, valName2, val2);
        ArgParserStringEnum<Integer> testee = new TestStringEnum(values, false);
        assertEquals(val, testee.enumMap.get(valName));
        assertEquals(val2, testee.enumMap.get(valName2));
        assertEquals(false, testee.allowNull);

        testee = new TestStringEnum(values, true);
        assertEquals(val, testee.enumMap.get(valName));
        assertEquals(val2, testee.enumMap.get(valName2));
        assertEquals(true, testee.allowNull);

        testee = new TestStringEnum(values);
        assertEquals(val, testee.enumMap.get(valName));
        assertEquals(val2, testee.enumMap.get(valName2));
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument() {
        Response<Object> unhandled = Response.notHandled();
        String valName = "name";
        int val = 42;
        Map<String, Integer> values = Map.of(valName, val);
        ArgParserStringEnum<Integer> testee = new TestStringEnum(values, false);

        // Ints don't work - needs to be an enum value
        assertFalse(testee.tryParseArgument(Response.is(5), "5").wasValueReturned());
        assertFalse(testee.tryParseArgument(Response.is("unknown"), "unknown").wasValueReturned());

        assertEquals(val, testee.tryParseNonNullArgument(unhandled, valName).getValue());
        assertEquals(val,
                testee.tryParseNonNullArgument(unhandled, "\"" + valName + "\"").getValue());

        assertFalse(testee.tryParseNonNullArgument(unhandled, "anything else").wasValueReturned());
    }

    @Test
    void tryParseNonNullArgument() {
        Response<Object> unhandled = Response.notHandled();
        String valName = "name";
        int val = 42;
        Map<String, Integer> values = Map.of(valName, val);
        ArgParserStringEnum<Integer> testee = new TestStringEnum(values, false);

        // Special value
        assertEquals(val, testee.tryParseNonNullArgument(unhandled, valName).getValue());
        assertEquals(val,
                testee.tryParseNonNullArgument(unhandled, "\"" + valName + "\"").getValue());

        assertFalse(testee.tryParseNonNullArgument(unhandled, "anything else").wasValueReturned());
    }
}
