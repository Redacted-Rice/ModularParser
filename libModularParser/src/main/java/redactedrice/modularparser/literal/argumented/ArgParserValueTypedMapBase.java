package redactedrice.modularparser.literal.argumented;


import java.util.Map;

import redactedrice.modularparser.core.Response;

public abstract class ArgParserValueTypedMapBase implements ArgumentParser {
    protected final boolean allowNull;

    protected ArgParserValueTypedMapBase(boolean allowNull) {
        this.allowNull = allowNull;
    }

    public Response<Object> tryParseArgument(Response<Object> parsed, String argument) {
        if ((parsed.wasHandled() && parsed.getValue() == null) || argument.isBlank() ||
                argument.equalsIgnoreCase("null")) {
            if (allowNull) {
                return Response.is(null);
            } else {
                return Response.error("Null value was passed but is not allowed");
            }
        }

        if (parsed.wasValueReturned() && parsed.getValue() instanceof Map<?, ?> map) {
            if (!map.values().stream().allMatch(v -> expectedType().isInstance(v))) {
                return Response
                        .error("Was not a map of expected type " + expectedType().getSimpleName());
            }
            return parsed;
        }
        return Response.notHandled();
    }

    protected abstract Class<?> expectedType();
}
