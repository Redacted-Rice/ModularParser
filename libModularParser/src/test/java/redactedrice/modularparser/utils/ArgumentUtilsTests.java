package redactedrice.modularparser.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgumentUtilsTests {

    @Test
    void argDichotomyToBool() {
        String getArgTrueName = "duplicatesT";
        String getArgFalseName = "duplicatesF";
        String getArgBadName = "bad";
        String getArgBadTypeName = "badType";
        String trueString = "allow";
        String falseString = "disallow";

        Map<String, Object> args = Map.of(getArgTrueName, trueString, getArgFalseName, falseString,
                getArgBadName, "bad val", getArgBadTypeName, 5);

        Response<Boolean> parsed = ArgumentUtils.argDichotomyToBool(getArgTrueName, args,
                trueString, falseString);
        assertTrue(parsed.wasValueReturned());
        assertTrue(parsed.getValue());

        parsed = ArgumentUtils.argDichotomyToBool(getArgFalseName, args, trueString, falseString);
        assertTrue(parsed.wasValueReturned());
        assertFalse(parsed.getValue());

        parsed = ArgumentUtils.argDichotomyToBool(getArgBadName, args, trueString, falseString);
        assertTrue(parsed.wasError());

        parsed = ArgumentUtils.argDichotomyToBool(getArgBadTypeName, args, trueString, falseString);
        assertTrue(parsed.wasError());
    }

    @Test
    void argToType() {
        String argIntName = "intField";
        String argStringName = "stringField";
        int intVal = 5;
        String strVal = "test string";

        Map<String, Object> args = Map.of(argIntName, intVal, argStringName, strVal);

        Response<Integer> intParsed = ArgumentUtils.argToType(argIntName, args, Integer.class);
        assertTrue(intParsed.wasValueReturned());
        assertEquals(intVal, intParsed.getValue());
        assertTrue(
                ArgumentUtils.argToType(argIntName, args, Integer.class, null).wasValueReturned());

        assertTrue(ArgumentUtils.argToType(argStringName, args, Integer.class).wasError());
        assertTrue(ArgumentUtils.argToType(argStringName, args, Integer.class, null).wasError());

        Response<String> strParsed = ArgumentUtils.argToType(argStringName, args, String.class);
        assertTrue(strParsed.wasValueReturned());
        assertEquals(strVal, strParsed.getValue());
        assertTrue(ArgumentUtils.argToType(argStringName, args, String.class, null)
                .wasValueReturned());
    }

    @Test
    void argToType_specialValues() {
        String argIntName = "intField";
        int intVal = 5;
        String argIntSpecialName = "intFieldSpecial";
        String specialValName = "unlimited";
        int specialVal = -1;
        String argIntBadName = "intFieldBad";

        Map<String, Object> args = Map.of(argIntName, intVal, argIntSpecialName, specialValName,
                argIntBadName, "Bad value");
        Map<Object, Integer> specialValues = Map.of(specialValName, specialVal);

        Response<Integer> intParsed = ArgumentUtils.argToType(argIntName, args, Integer.class,
                specialValues);
        assertTrue(intParsed.wasValueReturned());
        assertEquals(intVal, intParsed.getValue());

        intParsed = ArgumentUtils.argToType(argIntSpecialName, args, Integer.class, specialValues);
        assertTrue(intParsed.wasValueReturned());
        assertEquals(specialVal, intParsed.getValue());

        intParsed = ArgumentUtils.argToType(argIntBadName, args, Integer.class, specialValues);
        assertTrue(intParsed.wasError());
    }

    @Test
    void argToCollection() {
        String argCollectionName = "colField";
        String argArrayName = "arrField";
        String argStreamName = "streamField";
        String argIntName = "intField";
        Collection<Integer> colVal = List.of(1, 2, 3, 4);
        Boolean[] arrVal = {false, true, true};
        Collection<Character> streamCollection = List.of('t', 'e', 's', 't');
        int intVal = 5;

        Map<String, Object> args = Map.of(argCollectionName, colVal, argArrayName, arrVal,
                argStreamName, streamCollection.stream(), argIntName, intVal);

        Response<Collection<Object>> parsed = ArgumentUtils.argToCollection(argCollectionName,
                args);
        assertTrue(parsed.wasValueReturned());
        assertIterableEquals(colVal, parsed.getValue());

        parsed = ArgumentUtils.argToCollection(argArrayName, args);
        assertTrue(parsed.wasValueReturned());
        assertIterableEquals(List.of(arrVal), parsed.getValue());

        parsed = ArgumentUtils.argToCollection(argStreamName, args);
        assertTrue(parsed.wasValueReturned());
        assertIterableEquals(streamCollection, parsed.getValue());

        parsed = ArgumentUtils.argToCollection(argIntName, args);
        assertFalse(parsed.wasValueReturned());
    }

    @Test
    void argToStream() {
        String argCollectionName = "colField";
        String argArrayName = "arrField";
        String argStreamName = "streamField";
        String argIntName = "intField";
        Collection<Integer> colVal = List.of(1, 2, 3, 4);
        Boolean[] arrVal = {false, true, true};
        Collection<Character> streamCollection = List.of('t', 'e', 's', 't');
        int intVal = 5;

        Map<String, Object> args = Map.of(argCollectionName, colVal, argArrayName, arrVal,
                argStreamName, streamCollection.stream(), argIntName, intVal);

        Response<Stream<Object>> parsed = ArgumentUtils.argToStream(argCollectionName, args);
        assertTrue(parsed.wasValueReturned());
        assertIterableEquals(colVal, parsed.getValue().toList());

        parsed = ArgumentUtils.argToStream(argArrayName, args);
        assertTrue(parsed.wasValueReturned());
        assertIterableEquals(List.of(arrVal), parsed.getValue().toList());

        parsed = ArgumentUtils.argToStream(argStreamName, args);
        assertTrue(parsed.wasValueReturned());
        assertIterableEquals(streamCollection, parsed.getValue().toList());

        parsed = ArgumentUtils.argToStream(argIntName, args);
        assertFalse(parsed.wasValueReturned());
    }
}
