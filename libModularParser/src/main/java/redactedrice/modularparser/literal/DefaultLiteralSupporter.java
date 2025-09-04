package redactedrice.modularparser.literal;


import java.util.ArrayList;
import java.util.List;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.Response;

public class DefaultLiteralSupporter extends BaseModule implements LiteralSupporter {
    protected final List<LiteralParser> handlers = new ArrayList<>();
    protected final List<ChainableLiteralParser> chainedHandlers = new ArrayList<>();

    public DefaultLiteralSupporter() {
        super("DefaultLiteralSupportModule");
    }

    @Override
    public void handleModule(Module module) {
        if (module instanceof LiteralParser) {
            handlers.add((LiteralParser) module);
        }
        if (module instanceof ChainableLiteralParser) {
            chainedHandlers.add((ChainableLiteralParser) module);
        }
    }

    @Override
    public Response<Object> evaluateLiteral(String literal) {
        if (literal == null || literal.isBlank()) {
            return Response.error("Null or empty literal passed");
        }
        Response<Object> ret;
        for (LiteralParser lp : handlers) {
            ret = lp.tryParseLiteral(literal);
            if (ret.wasHandled()) {
                return ret;
            }
        }
        return Response.notHandled();
    }

    @Override
    public Response<Object> evaluateChainedLiteral(Object chained, String literal) {
        if (literal == null || literal.isBlank()) {
            return Response.error("Null or empty literal passed");
        }
        Response<Object> ret;
        for (ChainableLiteralParser clp : chainedHandlers) {
            ret = clp.tryEvaluateChainedLiteral(chained, literal);
            if (ret.wasHandled()) {
                return ret;
            }
        }
        return Response.notHandled();
    }
}
