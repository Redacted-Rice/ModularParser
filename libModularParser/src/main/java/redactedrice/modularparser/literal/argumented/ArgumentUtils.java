package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public class ArgumentUtils {
    private ArgumentUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String unquoteString(String input) {
        if (input == null || input.length() < 2)
            return input;
        if (input.startsWith("\"") && input.endsWith("\"")) {
            return input.substring(1, input.length() - 1);
        }
        return input;
    }

    public static String getUnquotedString(Response<Object> parsed, String argument) {
        if (parsed != null && parsed.wasValueReturned() &&
                parsed.getValue() instanceof String asString) {
            return unquoteString(asString);
        }
        return unquoteString(argument);
    }
}
