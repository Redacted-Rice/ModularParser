package redactedrice.modularparser;


public interface AliasModule extends LineHandlerModule {
	String replaceAliases(String line);
	boolean isAlias(String alias);
}
