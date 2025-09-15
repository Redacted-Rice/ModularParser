package redactedrice.modularparser.utils;


import java.util.Collection;
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
        return argToType(fieldName, args, clazz, null);
    }

    public static <T> Response<T> argToType(String fieldName, Map<String, Object> args,
            Class<T> clazz, Map<Object, T> specialValues) {
        Object val = args.get(fieldName);
        if (clazz.isInstance(val)) {
            return Response.is(clazz.cast(val));
        }
        if (specialValues != null) {
            if (specialValues.containsKey(val)) {
                return Response.is(specialValues.get(val));
            }
            return Response.error("Invalid type/special value: '" + clazz.getSimpleName()
                    + "' expected for '" + fieldName + "'. Special values: " + specialValues);
        }
        return Response.error(
                "Invalid type: '" + clazz.getSimpleName() + "' expected for '" + fieldName + "'");
    }

    public static Response<Stream<Object>> argToStream(String fieldName, Map<String, Object> args) {
        Object arg = args.get(fieldName);
        if (arg instanceof Stream<?> asStream) {
            return Response.is(asStream.filter(Object.class::isInstance).map(Object.class::cast));
        } else {
            Collection<Object> collection = ConversionUtils
                    .convertToCollection(args.get(fieldName));
            if (collection.size() <= 1) {
                return Response.error("Failed to get collection of '" + fieldName
                        + "'. Is it a valid list containing more than one object?");
            }
            return Response.is(collection.stream());
        }
    }

    public static Response<Collection<Object>> argToCollection(String fieldName,
            Map<String, Object> args) {
        Collection<Object> collection = ConversionUtils.convertToCollection(args.get(fieldName));
        if (collection.size() <= 1) {
            return Response.error("Failed to get collection of '" + fieldName
                    + "'. Is it a valid list containing more than one object?");
        }
        return Response.is(collection);
    }
}
