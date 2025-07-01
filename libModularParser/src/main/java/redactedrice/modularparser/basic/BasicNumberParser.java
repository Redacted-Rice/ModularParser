package redactedrice.modularparser.basic;


import java.util.Optional;

import redactedrice.modularparser.LiteralModule;

public class BasicNumberParser extends BaseModule implements LiteralModule {    
	public BasicNumberParser() {
		super("BasicNumberParser");
	};

	@Override
	public Optional<Object> tryEvaluateLiteral(String literal) {
        if (literal == null || literal.trim().isEmpty()) {
        	return Optional.empty();
        }
        String trimmed = literal.trim();
        
        try {
            return Optional.of(Integer.parseInt(trimmed));
        } catch (NumberFormatException e) {}

        try {
            return Optional.of(Long.parseLong(trimmed));
        } catch (NumberFormatException e) {}
        
        try {
            return Optional.of(Double.parseDouble(trimmed));
        } catch (NumberFormatException e) {}

        return Optional.empty();
	}
}
