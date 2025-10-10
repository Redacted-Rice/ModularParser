package redactedrice.modularparser.literal.argumented;


import java.util.HashMap;
import java.util.Map;

import redactedrice.modularparser.core.Response;

public class ArgParserIntSpecialVal extends ArgParserSingleType {
    protected final Map<String, Integer> specialValues;

    public ArgParserIntSpecialVal(String specialValName, int specialVal, boolean allowNull) {
        super(allowNull);
        specialValues = new HashMap<>();
        specialValues.put(specialValName, specialVal);
    }

    public ArgParserIntSpecialVal(String specialValName, int specialVal) {
        this(specialValName, specialVal, false);
    }

    public ArgParserIntSpecialVal(Map<String, Integer> specialVals, boolean allowNull) {
        super(allowNull);
        specialValues = new HashMap<>(specialVals);
    }

    public ArgParserIntSpecialVal(Map<String, Integer> specialVals) {
        this(specialVals, false);
    }

    @Override
    public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
        String unquoted = ArgumentUtils.getUnquotedString(parsed, argument);
        Integer val = specialValues.get(unquoted);
        if (val != null) {
            return Response.is(val);
        }
        return Response
                .error("Expected value of type Integer or special value (" + specialValues + ")");
    }

    @Override
    protected Class<?> expectedType() {
        return Integer.class;
    }
}
