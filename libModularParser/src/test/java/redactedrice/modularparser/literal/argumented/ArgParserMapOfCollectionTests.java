package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserMapOfCollectionTests {

    @Test
    void constuctor() {
        ArgParserMapOfCollection testee = new ArgParserMapOfCollection(true);
        assertEquals(true, testee.allowNull);

        testee = new ArgParserMapOfCollection(false);
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument() {
        ArgParserMapOfCollection testee = new ArgParserMapOfCollection(false);

        assertFalse(testee.tryParseArgument(Response.is(5), "5").wasValueReturned());
        assertFalse(testee.tryParseArgument(Response.is(Map.of("test", "val")), "5")
                .wasValueReturned());
        assertTrue(
                testee.tryParseArgument(Response.is(Map.of("test", List.of("val1", "val2"))), "5")
                        .wasValueReturned());
    }

    @Test
    void expectedType() {
        ArgParserMapOfCollection testee = new ArgParserMapOfCollection(true);
        assertEquals(Collection.class, testee.expectedType());
    }
}
