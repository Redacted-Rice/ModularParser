package redactedrice.modularparser;


import java.util.Optional;

public interface LiteralHandler {
	Optional<Object> tryEvaluateLiteral(String literal);
}
