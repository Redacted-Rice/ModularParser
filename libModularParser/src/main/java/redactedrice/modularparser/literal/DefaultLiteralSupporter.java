package redactedrice.modularparser.literal;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Module;

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
    public Object evaluateLiteral(String literal) {
        Optional<Object> ret;
        for (LiteralParser lp : handlers) {
            ret = lp.tryParseLiteral(literal);
            if (ret.isPresent()) {
                return ret.get();
            }
        }
        return null;
    }

    @Override
    public Object evaluateChainedLiteral(Object chained, String literal) {
        Optional<Object> ret;
        for (ChainableLiteralParser clp : chainedHandlers) {
            ret = clp.tryEvaluateChainedLiteral(chained, literal);
            if (ret.isPresent()) {
                return ret.get();
            }
        }
        return null;
    }
}
