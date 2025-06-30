package redactedrice.modularparser.basic;


public class LambdaModule extends LineStartMatchModule {
    private final ModuleLambdaFn handler;
	
	public LambdaModule(String name, ModuleLambdaFn handler, String... reservedWords) {
        super(name, reservedWords);
        this.handler = handler;
    }

	@Override
	public void handle(String logicalLine) {
		handler.handle(logicalLine);
	}
}
