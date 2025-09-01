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
    	assertEquals(testObj, testee.value());
    	assertTrue(testee.getError().isEmpty());
    	
    	// Test with null too
    	testObj = null;
    	testee = Response.is(testObj);
    	assertFalse(testee.wasNotHandled());
    	assertTrue(testee.wasHandled());
    	assertTrue(testee.wasValueReturned());
    	assertFalse(testee.wasError());
    	assertNull(testee.value());
    	assertTrue(testee.getError().isEmpty());
    }
    
    @Test
    void notHandledResponseTest() {
    	Response<Object> testee = Response.notHandled();
    	assertTrue(testee.wasNotHandled());
    	assertFalse(testee.wasHandled());
    	assertFalse(testee.wasValueReturned());
    	assertFalse(testee.wasError());
    	assertNull(testee.value());
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
    	assertNull(testee.value());
    	assertEquals(errorString, testee.getError());
    	
    	// Test an empty error string too
    	testee = Response.error("");
    	assertFalse(testee.wasNotHandled());
    	assertTrue(testee.wasHandled());
    	assertFalse(testee.wasValueReturned());
    	assertTrue(testee.wasError());
    	assertNull(testee.value());
    	assertEquals("", testee.getError());
    	
    	// Test a null error
    	testee = Response.error(null);
    	assertFalse(testee.wasNotHandled());
    	assertTrue(testee.wasHandled());
    	assertFalse(testee.wasValueReturned());
    	assertTrue(testee.wasError());
    	assertNull(testee.value());
    	assertEquals("", testee.getError());
    }
    
    @Test
    void equalsHashCodeTest() {
    	final String object = "test obj";
    	final String objectSame = "test obj";
    	final String objectDiff = "different obj";
    	Response<String> objectStr = Response.is(object);
    	Response<Object> objectObj = Response.is(object);
    	Response<String> objectSameStr = Response.is(objectSame);
    	Response<String> objectDiffStr = Response.is(objectDiff);
    	
    	Response<String> notHandledStr = Response.notHandled();
    	Response<Object> notHandledObj = Response.notHandled();
    	
    	final String errorMsg = "error";
    	Response<String> errorStr = Response.error(errorMsg);
    	Response<Object> errorObj = Response.error(errorMsg);
    	Response<String> errorStr2 = Response.error("a different error");

    	assertTrue(objectStr.equals(objectStr));
    	assertEquals(objectStr.hashCode(), objectStr.hashCode());
    	assertTrue(objectStr.equals(objectObj));
    	assertEquals(objectStr.hashCode(), objectObj.hashCode());
    	assertTrue(objectObj.equals(objectStr));
    	assertEquals(objectObj.hashCode(), objectStr.hashCode());
    	assertTrue(objectStr.equals(objectSameStr));
    	assertEquals(objectStr.hashCode(), objectSameStr.hashCode());
    	assertTrue(objectSameStr.equals(objectObj));
    	assertEquals(objectSameStr.hashCode(), objectObj.hashCode());
    	assertTrue(notHandledStr.equals(notHandledObj));
    	assertEquals(notHandledStr.hashCode(), notHandledObj.hashCode());
    	assertTrue(errorStr.equals(errorObj));
    	assertEquals(errorStr.hashCode(), errorObj.hashCode());
    	
    	assertFalse(objectStr.equals(null));
    	assertFalse(objectStr.equals("different type"));
    	assertFalse(objectObj.equals(objectDiffStr));
    	assertNotEquals(objectObj.hashCode(), objectDiffStr.hashCode());
    	assertFalse(errorStr.equals(errorStr2));
    	assertNotEquals(errorStr.hashCode(), errorStr2.hashCode());
    	assertFalse(objectStr.equals(notHandledStr));
    	assertNotEquals(objectStr.hashCode(), notHandledStr.hashCode());
    	assertFalse(objectStr.equals(errorStr));
    	assertNotEquals(objectStr.hashCode(), errorStr.hashCode());
    	assertFalse(notHandledStr.equals(objectStr));
    	assertNotEquals(notHandledStr.hashCode(), objectStr.hashCode());
    	assertFalse(notHandledStr.equals(errorStr));
    	assertNotEquals(notHandledStr.hashCode(), errorStr.hashCode());
    	assertFalse(errorStr.equals(objectStr));
    	assertNotEquals(errorStr.hashCode(), objectStr.hashCode());
    	assertFalse(errorStr.equals(notHandledStr));
    	assertNotEquals(errorStr.hashCode(), notHandledStr.hashCode());
    }
    
    @Test
    void toStringTest() {
    	assertEquals("Response(val=test object)", Response.is("test object").toString());
    	assertEquals("Response(not handled)", Response.notHandled().toString());
    	assertEquals("Response(error='')", Response.error("").toString());
    	assertEquals("Response(error='an error')", Response.error("an error").toString());
    }
}
