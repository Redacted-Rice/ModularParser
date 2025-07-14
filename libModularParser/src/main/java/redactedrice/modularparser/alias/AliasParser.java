package redactedrice.modularparser.alias;


import java.util.Set;

import redactedrice.modularparser.core.Module;

public interface AliasParser extends Module {
    String replaceAliases(String line);

    boolean isAlias(String alias);

    Set<String> getAliases();
}
