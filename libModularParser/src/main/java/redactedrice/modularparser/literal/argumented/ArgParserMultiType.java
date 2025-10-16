package redactedrice.modularparser.literal.argumented;


import java.util.Collection;

import redactedrice.modularparser.core.Response;

public abstract class ArgParserMultiType implements ArgumentParser {
    protected final boolean allowNull;

    protected ArgParserMultiType(boolean allowNull) {
        this.allowNull = allowNull;
    }

    public Response<Object> tryParseArgument(Response<Object> parsed, String argument) {
        if (parsed.wasValueReturned() &&
                expectedTypes().stream().anyMatch(e -> e.isInstance(parsed.getValue()))) {
            return modifyMatchingResponse(parsed);
        }

        if ((parsed.wasHandled() && parsed.getValue() == null) || argument.isBlank() ||
                argument.equalsIgnoreCase("null")) {
            if (allowNull) {
                return Response.is(null);
            } else {
                return Response.error("Null value was passed but is not allowed");
            }
        }
        Response<Object> response = tryParseNonNullArgument(parsed, argument);
        if (response.wasNotHandled()) {
            return Response.error("Passed arguement was of the wrong type! Expected: "
                    + expectedTypes().stream().map(Class::getSimpleName).toList() + " but found "
                    + parsed.getValue());
        }
        return response;
    }

    protected Response<Object> modifyMatchingResponse(Response<Object> matchingType) {
        return matchingType;
    }

    protected abstract Response<Object> tryParseNonNullArgument(Response<Object> parsed,
            String argument);

    protected abstract Collection<Class<?>> expectedTypes();
}
