package redactedrice.modularparser;


import java.util.Optional;

public interface LiteralModule extends Module {
	Optional<Object> tryEvaluateLiteral(String literal);
}
