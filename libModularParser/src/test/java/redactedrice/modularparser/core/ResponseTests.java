package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResponseTests {
    @Test
    void validResponseTest() {
        String testObj = "test string";
        Response<String> testee = Response.is(testObj);
        assertFalse(testee.wasNotHandled());
        assertTrue(testee.wasHandled());
        assertTrue(testee.wasValueReturned());
        assertFalse(testee.wasError());
        assertEquals(testObj, testee.getValue());
        assertTrue(testee.getError().isEmpty());

        // Test with null too
        testObj = null;
        testee = Response.is(testObj);
        assertFalse(testee.wasNotHandled());
        assertTrue(testee.wasHandled());
        assertTrue(testee.wasValueReturned());
        assertFalse(testee.wasError());
        assertNull(testee.getValue());
        assertTrue(testee.getError().isEmpty());
    }

    @Test
    void notHandledResponseTest() {
        Response<Object> testee = Response.notHandled();
        assertTrue(testee.wasNotHandled());
        assertFalse(testee.wasHandled());
        assertFalse(testee.wasValueReturned());
        assertFalse(testee.wasError());
        assertNull(testee.getValue());
        assertTrue(testee.getError().isEmpty());
    }

    @Test
    void errorResponseTest() {
        String errorString = "error string";
        Response<Object> testee = Response.error(errorString);
        assertFalse(testee.wasNotHandled());
        assertTrue(testee.wasHandled());
        assertFalse(testee.wasValueReturned());
        assertTrue(testee.wasError());
        assertNull(testee.getValue());
        assertEquals(errorString, testee.getError());

        // Test an empty error string too
        testee = Response.error("");
        assertFalse(testee.wasNotHandled());
        assertTrue(testee.wasHandled());
        assertFalse(testee.wasValueReturned());
        assertTrue(testee.wasError());
        assertNull(testee.getValue());
        assertEquals("", testee.getError());

        // Test a null error
        testee = Response.error(null);
        assertFalse(testee.wasNotHandled());
        assertTrue(testee.wasHandled());
        assertFalse(testee.wasValueReturned());
        assertTrue(testee.wasError());
        assertNull(testee.getValue());
        assertEquals("", testee.getError());
    }

    @Test
    void equalsHashCodeTest() {
        final String object = "test obj";
        final String objectSame = "test obj";
        Response<String> objectStr = Response.is(object);
        Response<Object> objectObj = Response.is(object);
        Response<String> objectSameStr = Response.is(objectSame);

        Response<String> notHandledStr = Response.notHandled();
        Response<Object> notHandledObj = Response.notHandled();

        final String errorMsg = "error";
        Response<String> errorStr = Response.error(errorMsg);
        Response<Object> errorObj = Response.error(errorMsg);

        // We are specifically testing the equals fn here so it makes sense to
        // call it instead of true as one side is the "expected" answer persay
        assertTrue(objectStr.equals(objectStr));// NOSONAR
        assertEquals(objectStr.hashCode(), objectStr.hashCode());
        assertTrue(objectStr.equals(objectObj));// NOSONAR
        assertEquals(objectStr.hashCode(), objectObj.hashCode());
        assertTrue(objectObj.equals(objectStr));// NOSONAR
        assertEquals(objectObj.hashCode(), objectStr.hashCode());
        assertTrue(objectStr.equals(objectSameStr));// NOSONAR
        assertEquals(objectStr.hashCode(), objectSameStr.hashCode());
        assertTrue(objectSameStr.equals(objectObj));// NOSONAR
        assertEquals(objectSameStr.hashCode(), objectObj.hashCode());
        assertTrue(notHandledStr.equals(notHandledObj));// NOSONAR
        assertEquals(notHandledStr.hashCode(), notHandledObj.hashCode());
        assertTrue(errorStr.equals(errorObj));// NOSONAR
        assertEquals(errorStr.hashCode(), errorObj.hashCode());
    }

    // Suppress unlikely warnings. That is intentionally part of what is being tested
    @SuppressWarnings("unlikely-arg-type")
    @Test
    void notEqualsHashCodeTest() {
        final String object = "test obj";
        final String objectDiff = "different obj";
        Response<String> objectStr = Response.is(object);
        Response<Object> objectObj = Response.is(object);
        Response<String> objectDiffStr = Response.is(objectDiff);

        Response<String> notHandledStr = Response.notHandled();

        final String errorMsg = "error";
        Response<String> errorStr = Response.error(errorMsg);
        Response<String> errorStr2 = Response.error("a different error");

        // We are specifically testing the equals fn here so it makes sense to
        // call it instead of true as one side is the "expected" answer persay
        assertFalse(objectStr.equals(null));// NOSONAR
        assertFalse(objectStr.equals("different type"));// NOSONAR
        assertFalse(objectObj.equals(objectDiffStr));// NOSONAR
        assertNotEquals(objectObj.hashCode(), objectDiffStr.hashCode());
        assertFalse(errorStr.equals(errorStr2));// NOSONAR
        assertNotEquals(errorStr.hashCode(), errorStr2.hashCode());
        assertFalse(objectStr.equals(notHandledStr));// NOSONAR
        assertNotEquals(objectStr.hashCode(), notHandledStr.hashCode());
        assertFalse(objectStr.equals(errorStr));// NOSONAR
        assertNotEquals(objectStr.hashCode(), errorStr.hashCode());
        assertFalse(notHandledStr.equals(objectStr));// NOSONAR
        assertNotEquals(notHandledStr.hashCode(), objectStr.hashCode());
        assertFalse(notHandledStr.equals(errorStr));// NOSONAR
        assertNotEquals(notHandledStr.hashCode(), errorStr.hashCode());
        assertFalse(errorStr.equals(objectStr));// NOSONAR
        assertNotEquals(errorStr.hashCode(), objectStr.hashCode());
        assertFalse(errorStr.equals(notHandledStr));// NOSONAR
        assertNotEquals(errorStr.hashCode(), notHandledStr.hashCode());
    }

    @Test
    void toStringTest() {
        assertEquals("Response(val=test object)", Response.is("test object").toString());
        assertEquals("Response(not handled)", Response.notHandled().toString());
        assertEquals("Response(error='')", Response.error("").toString());
        assertEquals("Response(error='an error')", Response.error("an error").toString());
    }
    
    @Test
    void castTest() {
    	String obj = "stringObj";
    	Response<Object> testee = Response.is(obj);
    	Response<String> casted = testee.convert(String.class);
    	assertTrue(casted.wasValueReturned());
    	assertEquals(obj, casted.getValue());
    	
    	assertNull(Response.is(null).convert(String.class).getValue());
    	assertTrue(Response.notHandled().convert(Integer.class).wasNotHandled());
    	assertTrue(testee.convert(Integer.class).wasError());

    	String error = "test";
    	testee = Response.error(error);
    	casted = testee.convert(String.class);
    	assertTrue(casted.wasError());
    	assertEquals(error, casted.getError());
    	
    }
}
