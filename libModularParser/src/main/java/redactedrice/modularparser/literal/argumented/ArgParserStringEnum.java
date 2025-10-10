package redactedrice.modularparser.literal.argumented;


import java.util.HashMap;
import java.util.Map;

import redactedrice.modularparser.core.Response;

public abstract class ArgParserStringEnum<T> extends ArgParserSingleType {
    protected final Map<String, T> enumMap;

    protected ArgParserStringEnum(Map<String, T> enumMap, boolean allowNull) {
        super(allowNull);
        this.enumMap = new HashMap<>(enumMap);
    }

    protected ArgParserStringEnum(Map<String, T> enumMap) {
        this(enumMap, false);
    }

    @Override
    public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
        String unquoted = ArgumentUtils.getUnquotedString(parsed, argument);
        T val = enumMap.get(unquoted);
        if (val != null) {
            return Response.is(val);
        }
        return Response.error("Passed value is undefined. Defined values: " + enumMap);
    }
}
