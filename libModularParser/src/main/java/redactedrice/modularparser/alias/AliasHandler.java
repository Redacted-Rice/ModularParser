package redactedrice.modularparser.alias;


import java.util.Set;

import redactedrice.modularparser.lineformer.LineModifier;

public interface AliasHandler extends LineModifier {
    String replaceAliases(String line);

    boolean isAlias(String alias);

    Set<String> getAliases();
}
