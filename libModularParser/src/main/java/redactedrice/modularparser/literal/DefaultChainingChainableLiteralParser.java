package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.lineformer.DefaultContinuerLineModifier;

public class DefaultChainingChainableLiteralParser extends BaseModule
        implements ChainableLiteralParser {
    protected final String chainingToken;

    protected LiteralSupporter literalHandler;

    public DefaultChainingChainableLiteralParser(ModularParser parser, String chainingToken) {
        super(parser, "BasicChainingParser");
        this.chainingToken = chainingToken;
        parser.addModule(new DefaultContinuerLineModifier(parser, "ChainingLiteralContinuerModule",
                chainingToken, false));
    }

    @Override
    public void setModuleRefs() {
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
