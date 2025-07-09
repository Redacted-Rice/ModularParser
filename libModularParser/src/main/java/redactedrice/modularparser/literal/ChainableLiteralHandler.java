package redactedrice.modularparser.literal;


import java.util.Optional;

public interface ChainableLiteralHandler extends LiteralHandler {
    Optional<Object> tryEvaluateChainedLiteral(Object chained, String literal);
}
