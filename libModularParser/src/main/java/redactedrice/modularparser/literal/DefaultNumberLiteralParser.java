package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.core.BaseModule;

public class DefaultNumberLiteralParser extends BaseModule implements LiteralParser {
    public DefaultNumberLiteralParser() {
        super("DefaultNumberParser");
    }

    protected enum PrimitiveType {
        INT, LONG, DOUBLE, UNSPECIFIED
    }

    @Override
    public Optional<Object> tryParseLiteral(String literal) {
        if (literal == null || literal.trim().isEmpty()) {
            return Optional.empty();
        }
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
        default -> Optional.empty();
        };
    }

    protected Optional<Object> parseWithType(String number, PrimitiveType type) {
        try {
            if (number.contains("e") || number.contains("E")) {
                // First parse it as a double to handle any E values
                double asDouble = Double.parseDouble(number);
                // Then check if its an int or a long and return those first if it is
                if (type == PrimitiveType.INT || type == PrimitiveType.UNSPECIFIED) {
                    int asInt = (int) asDouble;
                    if (asDouble == asInt) {
                        return Optional.of(asInt);
                    } else if (type == PrimitiveType.INT) {
                        return Optional.empty();
                    }
                }
                if (type == PrimitiveType.LONG || type == PrimitiveType.UNSPECIFIED) {
                    long asLong = (long) asDouble;
                    if (asDouble == asLong) {
                        return Optional.of(asLong);
                    } else if (type == PrimitiveType.LONG) {
                        return Optional.empty();
                    }
                }
                // Otherwise its a DOUBLE type or its unspecified but not a LONG or INT so it
                // must be a double
                return Optional.of(asDouble);
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        return parseAnyNonE(number, type);
    }

    protected Optional<Object> parseAnyNonE(String number, PrimitiveType type) {
        switch (type) {
        case INT:
            try {
                return Optional.of(Integer.parseInt(number));
            } catch (NumberFormatException e) {}
            break;
        case LONG:
            try {
                return Optional.of(Long.parseLong(number));
            } catch (NumberFormatException e) {}
            break;
        case DOUBLE:
            try {
                return Optional.of(Double.parseDouble(number));
            } catch (NumberFormatException e) {}
            break;
        default:
        case UNSPECIFIED:
            try {
                return Optional.of(Integer.parseInt(number));
            } catch (NumberFormatException e) {}

            try {
                return Optional.of(Long.parseLong(number));
            } catch (NumberFormatException e) {}

            try {
                return Optional.of(Double.parseDouble(number));
            } catch (NumberFormatException e) {}
        }
        return Optional.empty();
    }
}
