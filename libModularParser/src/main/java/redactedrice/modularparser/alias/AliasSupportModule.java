package redactedrice.modularparser.alias;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.Module;

public class AliasSupportModule extends BaseModule implements AliasSupporter {
    private final List<AliasHandler> handlers = new ArrayList<>();

    public AliasSupportModule() {
        super("VariableSupportModule");
    }

    @Override
    public void addAliasHandler(AliasHandler handler) {
    	handlers.add(handler);
        if (handler instanceof Module) {
            parser.addModule((Module) handler);
        }
    }

    @Override
    public boolean isAliasDefined(String alias) {
        for (AliasHandler aliasModule : handlers) {
            if (aliasModule.isAlias(alias)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getAllAliases() {
        Set<String> all = new HashSet<>();
        for (AliasHandler aliaser : handlers) {
            all.addAll(aliaser.getAliases());
        }
        return all;
    }
}
