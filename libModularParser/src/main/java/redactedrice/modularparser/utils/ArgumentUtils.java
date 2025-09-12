package redactedrice.modularparser.utils;


import java.util.Map;
import java.util.stream.Stream;

import redactedrice.modularparser.core.Response;
import redactedrice.reflectionhelpers.utils.ConversionUtils;

public class ArgumentUtils {
    private ArgumentUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Response<Boolean> argDichotomyToBool(String fieldName, Map<String, Object> args,
            String trueString, String falseString) {
        if (args.get(fieldName) instanceof String asString) {
            String lower = asString.toLowerCase();
            if (lower.equals(trueString)) {
                return Response.is(true);
            } else if (lower.equals(falseString)) {
                return Response.is(false);
            }
            return Response.error("Invalid value '" + asString + "' recieved for '" + fieldName
                    + "'. Expected either '" + trueString + "' or '" + falseString + "'");
        }
        return Response.error("Invalid type: Non string type passed to '" + fieldName
                + "'. Expected either '" + trueString + "' or '" + falseString + "'");
    }

    public static <T> Response<T> argToType(String fieldName, Map<String, Object> args,
            Class<T> clazz) {
        Object val = args.get(fieldName);
        if (clazz.isInstance(val)) {
            return Response.is(clazz.cast(val));
        }
        return Response.error(
                "Invalid type: '" + clazz.getSimpleName() + "' expected for '" + fieldName + "'");
    }

    public static Response<Stream<Object>> argToStream(String fieldName, Map<String, Object> args) {
        Object collection = args.get(fieldName);
        Stream<Object> stream = ConversionUtils.convertToStream(collection);
        if (stream.count() <= 1) {
            return Response.error("Failed to get stream of '" + fieldName
                    + "'. Is it a valid list containing more than one object?");
        }
        return Response.is(stream);
    }
}
