package redactedrice.modularparser.literal;


import java.util.Optional;

public interface LiteralHandler {
    public Optional<Object> tryEvaluateLiteral(String literal);
}
