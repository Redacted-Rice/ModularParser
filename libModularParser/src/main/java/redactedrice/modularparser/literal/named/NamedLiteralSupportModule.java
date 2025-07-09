package redactedrice.modularparser.literal.named;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.Module;

public class NamedLiteralSupportModule extends BaseModule implements NamedLiteralSupporter {
    private final List<NamedLiteralHandler> handlers = new ArrayList<>();

    public NamedLiteralSupportModule() {
        super("VariableSupportModule");
    }

    @Override
    public void addVariableParser(NamedLiteralHandler handler) {
    	handlers.add(handler);
        if (handler instanceof Module) {
            parser.addModule((Module) handler);
        }
    }

    @Override
    public boolean isVariableDefined(String var) {
        for (NamedLiteralHandler variableModule : handlers) {
            if (variableModule.isVariable(var)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> getAllVariables() {
        Map<String, Object> all = new HashMap<>();
        for (NamedLiteralHandler variables : handlers) {
            all.putAll(variables.getVariables());
        }
        return all;
    }
}
