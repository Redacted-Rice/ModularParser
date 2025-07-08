package redactedrice.modularparser.literals;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.Module;

public class LiteralSupportModule extends BaseModule implements LiteralSupporter {

    private final List<LiteralParser> parsers = new ArrayList<>();

    public LiteralSupportModule() {
        super("LiteralSupportModule");
    }

    @Override
    public void addLiteralParser(LiteralParser literalParser) {
        parsers.add(literalParser);
        if (literalParser instanceof Module) {
            parser.addModule((Module) literalParser);
        }
    }

    @Override
    public Object evaluateLiteral(String literal) {
        Optional<Object> ret;
        for (LiteralParser parser : parsers) {
            ret = parser.tryEvaluateLiteral(literal);
            if (ret.isPresent()) {
                return ret.get();
            }
        }
        return null;
    }
}
