package redactedrice.modularparser.literal.argumented;


import java.util.HashMap;
import java.util.Map;

import redactedrice.modularparser.core.Response;

public class ArgParserIntSpecialVal extends ArgumentParserBase {
	protected final Map<String, Integer> specialValues;
    
    public ArgParserIntSpecialVal(boolean allowNull, String specialValName, int specialVal) {
    	super(allowNull);
    	specialValues = new HashMap<>();
    	specialValues.put(specialValName, specialVal);
    }
    
    public ArgParserIntSpecialVal(boolean allowNull, Map<String, Integer> specialVals) {
    	super(allowNull);
    	specialValues = new HashMap<>(specialVals);
    }

	@Override
	public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
		if (parsed.wasValueReturned() && parsed.getValue() instanceof Integer) {
			return parsed;
		}
		String unquoted = ArgumentUtils.getUnquotedString(parsed, argument);
    	Integer val = specialValues.get(unquoted);
    	if (val != null) {
    		return Response.is(val);
    	}
        return Response.error("Expected value of type Integer or special value (" + specialValues + ")");
	}
}
