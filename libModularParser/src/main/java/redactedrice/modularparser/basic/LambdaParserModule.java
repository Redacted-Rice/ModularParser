package redactedrice.modularparser.basic;

public class LambdaParserModule extends BaseLineStartMatchModule {
    private final ModuleLambdaFn handler;

    public LambdaParserModule(String name, ModuleLambdaFn handler, String keyword) {
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
