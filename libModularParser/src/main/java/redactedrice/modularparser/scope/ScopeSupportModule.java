package redactedrice.modularparser.scope;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.Module;
import redactedrice.modularparser.literal.named.NamedLiteralHandler;

public class ScopeSupportModule extends BaseModule implements ScopeSupporter {
    private final List<ScopeHandler> handlers = new ArrayList<>();

    public ScopeSupportModule() {
        super("VariableSupportModule");
    }

    @Override
    public void addScopeHandler(ScopeHandler handler) {
        handlers.add(handler);
        if (handler instanceof Module) {
            parser.addModule((Module) handler);
        }
    }
}
