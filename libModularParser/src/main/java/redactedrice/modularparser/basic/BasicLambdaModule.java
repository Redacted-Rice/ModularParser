package redactedrice.modularparser.basic;

public class BasicLambdaModule extends LineStartMatchModule {
    private final ModuleLambdaFn handler;

    public BasicLambdaModule(String name, ModuleLambdaFn handler, String keyword) {
        super(name, keyword);
        this.handler = handler;
    }

    @Override
    public void handle(String logicalLine) {
        handler.handle(logicalLine);
    }
}
