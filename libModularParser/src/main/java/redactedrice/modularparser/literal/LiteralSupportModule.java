package redactedrice.modularparser.literal;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Module;

public class LiteralSupportModule extends BaseModule implements LiteralSupporter {
    private final List<LiteralParser> handlers = new ArrayList<>();
    private final List<ChainableLiteralParser> chainedHandlers = new ArrayList<>();

    public LiteralSupportModule() {
        super("LiteralSupportModule");
    }

    @Override
	public boolean handleModule(Module module) {
        if (module instanceof LiteralParser) {
        	handlers.add((LiteralParser) module);
        }
        if (module instanceof ChainableLiteralParser) {
        	chainedHandlers.add((ChainableLiteralParser) module);
        }
        return true;
    }

    @Override
    public Object evaluateLiteral(String literal) {
        Optional<Object> ret;
        for (LiteralParser parser : handlers) {
            ret = parser.tryParseLiteral(literal);
            if (ret.isPresent()) {
                return ret.get();
            }
        }
        return null;
    }

    @Override
    public Object evaluateChainedLiteral(Object chained, String literal) {
        Optional<Object> ret;
        for (ChainableLiteralParser parser : chainedHandlers) {
            ret = parser.tryEvaluateChainedLiteral(chained, literal);
            if (ret.isPresent()) {
                return ret.get();
            }
        }
        return null;
    }
}
