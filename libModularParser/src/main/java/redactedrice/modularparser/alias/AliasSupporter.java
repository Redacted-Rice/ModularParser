package redactedrice.modularparser.alias;

import java.util.Set;

import redactedrice.modularparser.core.Supporter;

public interface AliasSupporter extends Supporter {
    public boolean isAliasDefined(String var);
    public Set<String> getAllAliases();
}
