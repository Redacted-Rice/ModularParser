package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.BaseModule;

public class BasicNumberLiteralModule extends BaseModule implements LiteralHandler {
    public BasicNumberLiteralModule() {
        super("BasicNumberParser");
    }

    private enum PRIMITIVE_TYPE {
        INT, LONG, DOUBLE, UNSPECIFIED
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        if (literal == null || literal.trim().isEmpty())
            return Optional.empty();
        String trimmed = literal.trim();

        // Check for suffix
        char last = trimmed.charAt(trimmed.length() - 1);
        boolean hasSuffix = Character.isLetter(last) && trimmed.length() > 1;
        String suffix = hasSuffix ? String.valueOf(Character.toLowerCase(last)) : "";
        String number = hasSuffix ? trimmed.substring(0, trimmed.length() - 1) : trimmed;

        return switch (suffix) {
        case "i" -> parseWithType(number, PRIMITIVE_TYPE.INT);
        case "l" -> parseWithType(number, PRIMITIVE_TYPE.LONG);
        case "d" -> parseWithType(number, PRIMITIVE_TYPE.DOUBLE);
        default -> parseWithType(number, PRIMITIVE_TYPE.UNSPECIFIED);
        };
    }

    private Optional<Object> parseWithType(String number, PRIMITIVE_TYPE type) {
        try {
            if (number.contains("e") || number.contains("E")) {
                // First parse it as a double to handle any E values
                double asDouble = Double.parseDouble(number);
                // Then check if its an int or a long and return those first if it is
                if (type == PRIMITIVE_TYPE.INT || type == PRIMITIVE_TYPE.UNSPECIFIED) {
                    int asInt = (int) asDouble;
                    if (asDouble == asInt) {
                        return Optional.of(asInt);
                    } else if (type == PRIMITIVE_TYPE.INT) {
                        return Optional.empty();
                    }
                }
                if (type == PRIMITIVE_TYPE.LONG || type == PRIMITIVE_TYPE.UNSPECIFIED) {
                    long asLong = (long) asDouble;
                    if (asDouble == asLong) {
                        return Optional.of(asLong);
                    } else if (type == PRIMITIVE_TYPE.LONG) {
                        return Optional.empty();
                    }
                }
                // Otherwise its a DOUBLE type or its unspecified but not a LONG or INT so it
                // must be a double
                return Optional.of(asDouble);
            }
        } catch (NumberFormatException e) {}

        return parseAnyNonE(number, type);
    }

    private Optional<Object> parseAnyNonE(String number, PRIMITIVE_TYPE type) {
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
