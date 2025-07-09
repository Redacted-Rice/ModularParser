package redactedrice.modularparser.alias;

import java.util.Set;

public interface AliasSupporter {
	public void addAliasHandler(AliasHandler parser);
    public boolean isAliasDefined(String var);
    public Set<String> getAllAliases();
}
