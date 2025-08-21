package redactedrice.modularparser.literal;


import java.util.Optional;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.lineformer.DefaultContinuerLineModifier;

public class DefaultChainingChainableLiteralParser extends BaseModule
        implements ChainableLiteralParser {
    protected final String chainingToken;

    protected LiteralSupporter literalSupporter;

    public DefaultChainingChainableLiteralParser(String chainingToken, ModularParser parser) {
        super("DefaultChainingParser");
        this.chainingToken = chainingToken;
        parser.addModule(new DefaultContinuerLineModifier("ChainingLiteralContinuerModule",
                chainingToken, false));
    }

    @Override
    public void setModuleRefs() {
        super.setModuleRefs();
        literalSupporter = parser.getSupporterOfType(LiteralSupporter.class);
    }

    @Override
    public Optional<Object> tryParseLiteral(String literal) {
        if (literal == null || literal.isBlank()) {
            return Optional.empty();
        }

        int chainIdx = literal.indexOf(chainingToken);
        if (chainIdx < 0) {
            return Optional.empty();
        }

        Object evaluated = literalSupporter.evaluateLiteral(literal.substring(0, chainIdx).trim());
        return Optional.ofNullable(literalSupporter.evaluateChainedLiteral(evaluated,
                literal.substring(chainIdx + chainingToken.length()).trim()));
    }

    @Override
    public Optional<Object> tryEvaluateChainedLiteral(Object chained, String literal) {
        if (literal == null || literal.isBlank()) {
            return Optional.empty();
        }

        int chainIdx = literal.indexOf(chainingToken);
        if (chainIdx < 0) {
            return Optional.empty();
        }

        Object evaluated = literalSupporter.evaluateChainedLiteral(chained,
                literal.substring(0, chainIdx).trim());
        return Optional.ofNullable(literalSupporter.evaluateChainedLiteral(evaluated,
                literal.substring(chainIdx + chainingToken.length()).trim()));
    }

}
