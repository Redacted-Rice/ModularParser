package redactedrice.modularparser.literals;


import java.util.Optional;

public interface ChainableLiteralParser extends LiteralParser {
    Optional<Object> tryEvaluateChainedLiteral(Object chained, String literal);
}
