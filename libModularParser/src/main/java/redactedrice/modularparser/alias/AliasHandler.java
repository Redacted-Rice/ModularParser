package redactedrice.modularparser.alias;


import java.util.Set;

public interface AliasHandler {
    String replaceAliases(String line);

    boolean isAlias(String alias);

    Set<String> getAliases();
}
