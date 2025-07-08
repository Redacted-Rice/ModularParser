package redactedrice.modularparser.literals;


import java.util.Optional;

public interface LiteralParser {
    public Optional<Object> tryEvaluateLiteral(String literal);
}
