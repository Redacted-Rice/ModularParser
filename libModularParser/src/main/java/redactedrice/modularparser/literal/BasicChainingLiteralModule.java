package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.BaseModule;

public class BasicChainingLiteralModule extends BaseModule implements ChainableLiteralHandler {
    protected final String chainingToken;

    protected LiteralSupporter literalHandler;

    public BasicChainingLiteralModule(String chainingToken) {
        super("BasicChainingParser");
        this.chainingToken = chainingToken;
    }

    @Override
    public void configure() {
        parser.addLineContinue(chainingToken, false);
        literalHandler = parser.getSupporterOfType(LiteralSupporter.class);
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        int chainIdx = literal.indexOf(chainingToken);
        if (chainIdx < 0) {
            return Optional.empty();
        }

        Object evaluated = literalHandler.evaluateLiteral(literal.substring(0, chainIdx).trim());
        return Optional.ofNullable(literalHandler.evaluateChainedLiteral(evaluated, 
        		literal.substring(chainIdx + chainingToken.length()).trim()));
    }

	@Override
	public Optional<Object> tryEvaluateChainedLiteral(Object chained, String literal) {
        int chainIdx = literal.indexOf(chainingToken);
        if (chainIdx < 0) {
            return Optional.empty();
        }

        Object evaluated = literalHandler.evaluateChainedLiteral(chained, literal.substring(0, chainIdx).trim());
        return Optional.ofNullable(literalHandler.evaluateChainedLiteral(evaluated, 
        		literal.substring(chainIdx + chainingToken.length()).trim()));
	}

}
