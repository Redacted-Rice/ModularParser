package redactedrice.modularparser.alias;


import java.util.HashSet;
import java.util.Set;

import redactedrice.modularparser.core.BaseSupporter;

public class AliasSupportModule extends BaseSupporter<AliasParser> implements AliasSupporter {
    public AliasSupportModule() {
        super("AliasSupportModule", AliasParser.class);
    }

    @Override
    public boolean isAliasDefined(String alias) {
        for (AliasParser aliasModule : submodules) {
            if (aliasModule.isAlias(alias)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getAllAliases() {
        Set<String> all = new HashSet<>();
        for (AliasParser aliaser : submodules) {
            all.addAll(aliaser.getAliases());
        }
        return all;
    }
}
