package redactedrice.modularparser;


public interface VariableModule extends LineHandlerModule, LiteralModule {
	boolean isVariable(String var);
}
