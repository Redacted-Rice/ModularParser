package redactedrice.modularparser.basic;


public class BasicLambdaModule extends LineStartMatchModule {
    private final ModuleLambdaFn handler;
	
	public BasicLambdaModule(String name, ModuleLambdaFn handler, String... reservedWords) {
        super(name, reservedWords);
        this.handler = handler;
    }

	@Override
	public void handle(String logicalLine) {
		handler.handle(logicalLine);
	}
}
