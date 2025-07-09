package redactedrice.modularparser.variable;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.Module;

public class VariableSupportModule extends BaseModule implements VariableSupporter {
    private final List<VariableHandler> handlers = new ArrayList<>();

    public VariableSupportModule() {
        super("VariableSupportModule");
    }

    @Override
    public void addVariableParser(VariableHandler handler) {
    	handlers.add(handler);
        if (handler instanceof Module) {
            parser.addModule((Module) handler);
        }
    }

    @Override
    public boolean isVariableDefined(String var) {
        for (VariableHandler variableModule : handlers) {
            if (variableModule.isVariable(var)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> getAllVariables() {
        Map<String, Object> all = new HashMap<>();
        for (VariableHandler variables : handlers) {
            all.putAll(variables.getVariables());
        }
        return all;
    }
}
