package redactedrice.modularparser.literals;


import java.util.List;
import java.util.Optional;

import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.Module;

public class BasicChainingParser extends BaseModule implements LiteralParser {
    protected final String chainingToken;

    protected LiteralSupporter literalHandler;

    public BasicChainingParser(String chainingToken) {
        super("BasicChainingParser");
        this.chainingToken = chainingToken;
    }

    @Override
    public void configure() {
        parser.addLineContinue(chainingToken, false);

        List<Module> literalSupporters = parser.getModulesOfType(LiteralSupporter.class);
        if (literalSupporters.size() != 1) {
            throw new RuntimeException("Temp");
        }
        literalHandler = (LiteralSupporter) literalSupporters.get(0);
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        int chainIdx = literal.indexOf(chainingToken);
        if (chainIdx < 0) {
            return Optional.empty();
        }

        return Optional
                .ofNullable(literalHandler.evaluateLiteral(literal.substring(0, chainIdx).trim()));
    }

}
