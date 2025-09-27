package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.testsupport.SimpleObjectArgumentParser;

class ArgumentsDefinitionTests {
	private final String arg1 = "arg1";
	private final String arg2 = "arg2";
	private final String arg3 = "arg3";
	private final Object default1 = 5;
	private final Object default2 = "test";
	private final Object default3 = false;
	
    @Test
    void constuctor_required() {
    	ArgumentsDefinition testee = new ArgumentsDefinition(
    			new String[] {arg1, arg2, arg3}, null, null, null);
        assertEquals(3, testee.getNumRequiredArgs());
        assertEquals(0, testee.getNumOptionalArgs());
        assertEquals(3, testee.getNumArgs());

        assertNull(testee.getRequiredArg(-1));
        assertEquals(arg1, testee.getRequiredArg(0));
        assertEquals(arg2, testee.getRequiredArg(1));
        assertEquals(arg3, testee.getRequiredArg(2));
        assertNull(testee.getRequiredArg(3));
        
        assertNull(testee.getArg(-1));
        assertEquals(arg1, testee.getArg(0));
        assertEquals(arg2, testee.getArg(1));
        assertEquals(arg3, testee.getArg(2));
        assertNull(testee.getArg(3));
    }
	
    @Test
    void constuctor_optional() {
    	ArgumentsDefinition testee = new ArgumentsDefinition(
    			null, new String[] {arg1, arg2, arg3}, new Object[] {default1, default2, default3}, null);
        assertEquals(0, testee.getNumRequiredArgs());
        assertEquals(3, testee.getNumOptionalArgs());
        assertEquals(3, testee.getNumArgs());

        assertNull(testee.getOptionalArg(-1));
        assertEquals(arg1, testee.getOptionalArg(0));
        assertEquals(arg2, testee.getOptionalArg(1));
        assertEquals(arg3, testee.getOptionalArg(2));
        assertNull(testee.getOptionalArg(3));
        
        assertNull(testee.getArg(-1));
        assertEquals(arg1, testee.getArg(0));
        assertEquals(arg2, testee.getArg(1));
        assertEquals(arg3, testee.getArg(2));
        assertNull(testee.getArg(3));
        
        assertNull(testee.getOptionalDefault(-1));
        assertEquals(default1, testee.getOptionalDefault(0));
        assertEquals(default2, testee.getOptionalDefault(1));
        assertEquals(default3, testee.getOptionalDefault(2));
        assertNull(testee.getOptionalDefault(3));
        
        assertThrows(IllegalArgumentException.class, () -> {
        	new ArgumentsDefinition(
        			null, new String[] {arg1, arg2}, new Object[] {default1, default2, default3}, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
        	new ArgumentsDefinition(
        			null, new String[] {arg1, arg2, arg3}, new Object[] {default1, default2}, null);
        });
    }	
    
    @Test
    void constuctor_mixed() {
    	ArgumentsDefinition testee = new ArgumentsDefinition(
    			new String[] {arg1, arg2}, new String[] {arg3}, new Object[] {default3}, null);
        assertEquals(2, testee.getNumRequiredArgs());
        assertEquals(1, testee.getNumOptionalArgs());
        assertEquals(3, testee.getNumArgs());

        assertNull(testee.getRequiredArg(-1));
        assertEquals(arg1, testee.getRequiredArg(0));
        assertEquals(arg2, testee.getRequiredArg(1));
        assertNull(testee.getRequiredArg(2));
        
        assertNull(testee.getOptionalArg(-1));
        assertEquals(arg3, testee.getOptionalArg(0));
        assertNull(testee.getOptionalArg(1));
        
        assertNull(testee.getArg(-1));
        assertEquals(arg1, testee.getArg(0));
        assertEquals(arg2, testee.getArg(1));
        assertEquals(arg3, testee.getArg(2));
        assertNull(testee.getArg(3));
        
        assertNull(testee.getOptionalDefault(-1));
        assertEquals(default3, testee.getOptionalDefault(0));
        assertNull(testee.getOptionalDefault(1));
    }
    
    @Test
    void constuctor_allNull() {
    	ArgumentsDefinition testee = new ArgumentsDefinition();
        assertEquals(0, testee.getNumRequiredArgs());
        assertEquals(0, testee.getNumOptionalArgs());
        assertEquals(0, testee.getNumArgs());
        
    	testee = new ArgumentsDefinition(null, null, null, null);
        assertEquals(0, testee.getNumRequiredArgs());
        assertEquals(0, testee.getNumOptionalArgs());
        assertEquals(0, testee.getNumArgs());
    }
    
    @Test
    void constuctor_argParsers() {
    	TypeAny type1 = new TypeAny(true);
    	TypeEnforcer<Integer> type2 = new TypeEnforcer<>(false, Integer.class);
    	SimpleObjectArgumentParser type3 = new SimpleObjectArgumentParser();
    	
    	ArgumentsDefinition testee = new ArgumentsDefinition(
    			new String[] {arg1, arg2}, new String[] {arg3}, new Object[] {default3}, 
    			new ArgumentParser[] {type1, type2, type3});
        assertEquals(2, testee.getNumRequiredArgs());
        assertEquals(1, testee.getNumOptionalArgs());
        assertEquals(3, testee.getNumArgs());

        assertEquals(type1, testee.getArgParser(arg1));
        assertEquals(type2, testee.getArgParser(arg2));
        assertEquals(type3, testee.getArgParser(arg3));
        assertNull(testee.getArgParser("bad value"));
        assertNull(testee.getArgParser(null));
        
        assertThrows(IllegalArgumentException.class, () -> {
        	new ArgumentsDefinition(
        			new String[] {arg1, arg2}, null, null, new ArgumentParser[] {type1, type2, type3});
        });
        assertThrows(IllegalArgumentException.class, () -> {
        	new ArgumentsDefinition(
        			new String[] {arg1, arg2, arg3}, null, null, new ArgumentParser[] {type1, type2});
        });
    }
}
