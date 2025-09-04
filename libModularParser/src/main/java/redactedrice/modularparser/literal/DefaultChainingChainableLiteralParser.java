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
        return tryEvaluateInternal(literal, false, null);
    }

    @Override
    public Response<Object> tryEvaluateChainedLiteral(Object chained, String literal) {
        return tryEvaluateInternal(literal, true, chained);
    }

    protected Response<Object> tryEvaluateInternal(String literal, boolean isChained,
            Object chained) {
        int chainIdx = literal.indexOf(chainingToken);
        if (chainIdx < 0) {
            return Response.notHandled();
        }

        String evalFirst;
        String evalSecond;
        if (queueNotStack) {
            evalFirst = literal.substring(0, chainIdx).trim();
            evalSecond = literal.substring(chainIdx + chainingToken.length()).trim();
        } else {
            evalFirst = literal.substring(chainIdx + chainingToken.length()).trim();
            evalSecond = literal.substring(0, chainIdx).trim();
        }

        Response<Object> evaluated;
        if (isChained) {
            evaluated = literalSupporter.evaluateChainedLiteral(chained, evalFirst);
        } else {
            evaluated = literalSupporter.evaluateLiteral(evalFirst);
        }
        if (evaluated.wasNotHandled()) {
            return Response.error("Failed to parse first literal " + evalFirst);
        } else if (evaluated.wasError()) {
            return Response.error("Error parsing first literal:" + evaluated.getError());
        }
        evaluated = literalSupporter.evaluateChainedLiteral(evaluated.value(), evalSecond);
        if (evaluated.wasNotHandled()) {
            return Response.error("Failed to parse second literal " + evalSecond);
        } else if (evaluated.wasError()) {
            return Response.error("Error parsing second literal:" + evaluated.getError());
        }
        return evaluated;
    }
}
