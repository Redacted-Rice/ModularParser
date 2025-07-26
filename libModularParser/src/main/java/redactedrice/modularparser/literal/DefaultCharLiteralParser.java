package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.ModularParser;

public class DefaultCharLiteralParser extends BaseModule implements LiteralParser {
    public DefaultCharLiteralParser(ModularParser parser) {
        super(parser, "CharParser");
    }

    @Override
    public Optional<Object> tryParseLiteral(String literal) {
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
