package redactedrice.modularparser.lineparser;

import redactedrice.modularparser.core.ModularParser;

public class DefaultLambdaParser extends BaseLineStartMatcher {
    private final DefaultLambdaParserFn handler;

    public DefaultLambdaParser(ModularParser parser, String name, DefaultLambdaParserFn handler, String keyword) {
        super(parser, name, keyword);
        this.handler = handler;
    }

    @Override
    public boolean tryParseLine(String logicalLine) {
        if (!matches(logicalLine)) {
            return false;
        }
        handler.handle(logicalLine);
        return true;
    }
}
