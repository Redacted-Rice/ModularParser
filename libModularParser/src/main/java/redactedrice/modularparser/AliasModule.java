package redactedrice.modularparser;


public interface AliasModule extends ParserModule {
	String replaceAliases(String line);
	boolean isAlias(String alias);
}
