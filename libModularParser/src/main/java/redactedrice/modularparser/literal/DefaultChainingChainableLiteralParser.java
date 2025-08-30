package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.DefaultContinuerLineModifier;

public class DefaultChainingChainableLiteralParser extends BaseModule
        implements ChainableLiteralParser {
    protected final String chainingToken;
    protected final boolean queueNotStack;

    protected LiteralSupporter literalSupporter;

    public DefaultChainingChainableLiteralParser(String name, String chainingToken,
            boolean queueNotStack, ModularParser parser) {
        super(name);
        this.chainingToken = chainingToken;
        this.queueNotStack = queueNotStack;
        parser.addModule(
                new DefaultContinuerLineModifier(name + "Continuer", chainingToken, false));
    }

    @Override
    public void setModuleRefs() {
        super.setModuleRefs();
        literalSupporter = parser.getSupporterOfType(LiteralSupporter.class);
    }

    @Override
    public Response<Object> tryParseLiteral(String literal) {
        int chainIdx = literal.indexOf(chainingToken);
        if (chainIdx < 0) {
            return Response.notHandled();
        }

        String evalFirst;
        String evalSecond;
        if (queueNotStack) {
            evalFirst = literal.substring(chainIdx + chainingToken.length()).trim();
            evalSecond = literal.substring(0, chainIdx).trim();
        } else {
            evalFirst = literal.substring(0, chainIdx).trim();
            evalSecond = literal.substring(chainIdx + chainingToken.length()).trim();
        }
        Object evaluated = literalSupporter.evaluateLiteral(evalFirst);
        return Response.is(literalSupporter.evaluateChainedLiteral(evaluated, evalSecond));
    }

    @Override
    public Response<Object> tryEvaluateChainedLiteral(Object chained, String literal) {
        if (literal == null || literal.isBlank()) {
            return Response.notHandled();
        }

        int chainIdx = literal.indexOf(chainingToken);
        if (chainIdx < 0) {
            return Response.notHandled();
        }

        Object evaluated = literalSupporter.evaluateChainedLiteral(chained,
                literal.substring(0, chainIdx).trim());
        return Response.is(literalSupporter.evaluateChainedLiteral(evaluated,
                literal.substring(chainIdx + chainingToken.length()).trim()));
    }

}
