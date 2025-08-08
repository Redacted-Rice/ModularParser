package redactedrice.modularparser.lineparser;


public class DefaultLambdaParser extends BaseLineStartMatcher {
    protected final DefaultLambdaParserFn handler;

    public DefaultLambdaParser(String name, DefaultLambdaParserFn handler, String keyword) {
        super(name, keyword);
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
