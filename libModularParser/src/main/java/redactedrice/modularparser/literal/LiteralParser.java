package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.core.Module;

public interface LiteralParser extends Module {
    public Optional<Object> tryParseLiteral(String literal);
}
