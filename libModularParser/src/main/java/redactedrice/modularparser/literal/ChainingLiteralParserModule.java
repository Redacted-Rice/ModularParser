package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.lineformer.LineContinuerLineModifierModule;

public class ChainingLiteralParserModule extends BaseModule implements ChainableLiteralParser {
    protected final String chainingToken;

    protected LiteralSupporter literalHandler;

    public ChainingLiteralParserModule(String chainingToken, ModularParser parser) {
        super("BasicChainingParser");
        this.chainingToken = chainingToken;
        parser.addModule(new LineContinuerLineModifierModule("ChainingLiteralContinuerModule",
                chainingToken, false));
    }

    @Override
    public void configure() {
        literalHandler = parser.getSupporterOfType(LiteralSupporter.class);
    }

    @Override
    public Optional<Object> tryParseLiteral(String literal) {
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

        Object evaluated = literalHandler.evaluateChainedLiteral(chained,
                literal.substring(0, chainIdx).trim());
        return Optional.ofNullable(literalHandler.evaluateChainedLiteral(evaluated,
                literal.substring(chainIdx + chainingToken.length()).trim()));
    }

}
