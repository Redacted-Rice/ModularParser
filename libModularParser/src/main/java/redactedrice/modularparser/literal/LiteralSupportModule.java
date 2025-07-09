package redactedrice.modularparser.literal;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.Module;

public class LiteralSupportModule extends BaseModule implements LiteralSupporter {
    private final List<LiteralHandler> handlers = new ArrayList<>();
    private final List<ChainableLiteralHandler> chainedHandlers = new ArrayList<>();

    public LiteralSupportModule() {
        super("LiteralSupportModule");
    }

    @Override
    public void addLiteralParser(LiteralHandler literalParser) {
    	handlers.add(literalParser);
        if (literalParser instanceof ChainableLiteralHandler) {
        	chainedHandlers.add((ChainableLiteralHandler) literalParser);
        }
        if (literalParser instanceof Module) {
            parser.addModule((Module) literalParser);
        }
    }

    @Override
    public Object evaluateLiteral(String literal) {
        Optional<Object> ret;
        for (LiteralHandler parser : handlers) {
            ret = parser.tryEvaluateLiteral(literal);
            if (ret.isPresent()) {
                return ret.get();
            }
        }
        return null;
    }

    @Override
    public Object evaluateChainedLiteral(Object chained, String literal) {
        Optional<Object> ret;
        for (ChainableLiteralHandler parser : chainedHandlers) {
            ret = parser.tryEvaluateChainedLiteral(chained, literal);
            if (ret.isPresent()) {
                return ret.get();
            }
        }
        return null;
    }
}
