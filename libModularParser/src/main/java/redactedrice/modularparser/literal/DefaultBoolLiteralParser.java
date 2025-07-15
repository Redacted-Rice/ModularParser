package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.core.BaseModule;

public class DefaultBoolLiteralParser extends BaseModule implements LiteralParser {
    public DefaultBoolLiteralParser() {
        super("BoolParser");
    }

    @Override
    public Optional<Object> tryParseLiteral(String literal) {
        if (literal == null) {
            return Optional.empty();
        }

        String trimmed = literal.trim().toLowerCase();
        return switch (trimmed) {
        case "true", "t" -> Optional.of(true);
        case "false", "f" -> Optional.of(false);
        default -> Optional.empty();
        };
    }
}
