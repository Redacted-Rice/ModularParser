package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.BaseModule;

public class BasicCharLiteralModule extends BaseModule implements LiteralHandler {
    public BasicCharLiteralModule() {
        super("BasicCharParser");
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        if (literal == null || literal.trim().isEmpty()) {
            return Optional.empty();
        }

        String trimmed = literal.trim();

        // Double-quoted string
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            String body = trimmed.substring(1, trimmed.length() - 1);
            return Optional.of(body);
        }

        // Single-quoted char
        if (trimmed.length() >= 3 && trimmed.startsWith("'") && trimmed.endsWith("'")) {
            String body = trimmed.substring(1, trimmed.length() - 1);
            if (body.length() == 1) {
                return Optional.of(body.charAt(0));
            }
        }

        return Optional.empty();
    }
}
