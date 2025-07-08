package redactedrice.modularparser.literals;


import java.util.Optional;

import redactedrice.modularparser.BaseModule;

public class BasicBoolParser extends BaseModule implements LiteralParser {
    public BasicBoolParser() {
        super("BasicBoolParser");
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
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
