package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserSpecialValTests {

    @Test
    void constuctor() {
        String valName = "name";
        int val = 42;
        ArgParserIntSpecialVal testee = new ArgParserIntSpecialVal(valName, val, true);
        assertEquals(val, testee.specialValues.get(valName));
        assertEquals(true, testee.allowNull);

        testee = new ArgParserIntSpecialVal(valName, val);
        assertEquals(val, testee.specialValues.get(valName));
        assertEquals(false, testee.allowNull);

        String valName2 = "name2";
        int val2 = 5;
        Map<String, Integer> specialVals = Map.of(valName, val, valName2, val2);
        testee = new ArgParserIntSpecialVal(specialVals, true);
        assertEquals(val, testee.specialValues.get(valName));
        assertEquals(val2, testee.specialValues.get(valName2));
        assertEquals(true, testee.allowNull);

        testee = new ArgParserIntSpecialVal(specialVals);
        assertEquals(val, testee.specialValues.get(valName));
        assertEquals(val2, testee.specialValues.get(valName2));
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument() {
        String valName = "name";
        int val = 42;
        ArgParserIntSpecialVal testee = new ArgParserIntSpecialVal(valName, val, false);

        // Test some ints - these will be handled earlier
        assertEquals(5, testee.tryParseArgument(Response.is(5), "5").getValue());
        assertEquals(-3, testee.tryParseArgument(Response.is(-3), "-3").getValue());
        assertEquals(val,
                testee.tryParseNonNullArgument(Response.notHandled(), valName).getValue());
    }

    @Test
    void tryParseNonNullArgument() {
        Response<Object> unhandled = Response.notHandled();
        String valName = "name";
        int val = 42;
        ArgParserIntSpecialVal testee = new ArgParserIntSpecialVal(valName, val, false);

        // Special value
        assertEquals(val, testee.tryParseNonNullArgument(unhandled, valName).getValue());
        assertEquals(val,
                testee.tryParseNonNullArgument(unhandled, "\"" + valName + "\"").getValue());

        assertFalse(testee.tryParseNonNullArgument(unhandled, "anything else").wasValueReturned());
    }

    @Test
    void expectedType() {
        ArgParserIntSpecialVal testee = new ArgParserIntSpecialVal("name", 42, false);
        assertEquals(Integer.class, testee.expectedType());
    }
}
