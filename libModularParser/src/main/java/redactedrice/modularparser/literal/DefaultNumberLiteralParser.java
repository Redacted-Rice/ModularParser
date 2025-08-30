package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Response;

public class DefaultNumberLiteralParser extends BaseModule implements LiteralParser {
    public DefaultNumberLiteralParser() {
        super("DefaultNumberParser");
    }

    protected enum PrimitiveType {
        INT, LONG, DOUBLE, UNSPECIFIED
    }

    @Override
    public Response<Object> tryParseLiteral(String literal) {
        String trimmed = literal.trim();

        // Check for suffix
        char last = trimmed.charAt(trimmed.length() - 1);
        boolean hasSuffix = Character.isLetter(last);
        String suffix = hasSuffix ? String.valueOf(Character.toLowerCase(last)) : "unspecified";
        String number = hasSuffix ? trimmed.substring(0, trimmed.length() - 1) : trimmed;

        return switch (suffix) {
        case "i" -> parseWithType(number, PrimitiveType.INT);
        case "l" -> parseWithType(number, PrimitiveType.LONG);
        case "d" -> parseWithType(number, PrimitiveType.DOUBLE);
        case "unspecified" -> parseWithType(number, PrimitiveType.UNSPECIFIED);
        default -> Response.notHandled();
        };
    }

    protected Response<Object> parseWithType(String number, PrimitiveType type) {
        try {
            if (number.contains("e") || number.contains("E")) {
                // First parse it as a double to handle any E values
                double asDouble = Double.parseDouble(number);
                // Then check if its an int or a long and return those first if it is
                if (type == PrimitiveType.INT || type == PrimitiveType.UNSPECIFIED) {
                    int asInt = (int) asDouble;
                    if (asDouble == asInt) {
                        return Response.is(asInt);
                    } else if (type == PrimitiveType.INT) {
                        return Response.error("value \"" + number + 
                        		"\" was specified as an int but failed to be parsed as an int");
                    }
                }
                if (type == PrimitiveType.LONG || type == PrimitiveType.UNSPECIFIED) {
                    long asLong = (long) asDouble;
                    if (asDouble == asLong) {
                        return Response.is(asLong);
                    } else if (type == PrimitiveType.LONG) {
                        return Response.error("value \"" + number + 
                        		"\" was specified as an long but failed to be parsed as an long");
                    }
                }
                // Otherwise its a DOUBLE type or its unspecified but not a LONG or INT so it
                // must be a double
                return Response.is(asDouble);
            }
        } catch (NumberFormatException e) {
        	// Wasn't a number, let other parsers try
            return Response.notHandled();
        }
        return parseAnyNonE(number, type);
    }

    protected Response<Object> parseAnyNonE(String number, PrimitiveType type) {
        switch (type) {
        case INT:
            try {
                return Response.is(Integer.parseInt(number));
            } catch (NumberFormatException e) {
            	break;
            }
        case LONG:
            try {
                return Response.is(Long.parseLong(number));
            } catch (NumberFormatException e) {
            	break;
            }
        case DOUBLE:
            try {
                return Response.is(Double.parseDouble(number));
            } catch (NumberFormatException e) {
            	break;
            }
        default:
        case UNSPECIFIED:
            try {
                return Response.is(Integer.parseInt(number));
            } catch (NumberFormatException e) {
            	// try next type
            }

            try {
                return Response.is(Long.parseLong(number));
            } catch (NumberFormatException e) {
            	// try next type
            }

            try {
                return Response.is(Double.parseDouble(number));
            } catch (NumberFormatException e) {
            	// nothing else to try
            	break;
            }
        }
    	// Wasn't a number, let other parsers try
        return Response.notHandled();
    }
}
