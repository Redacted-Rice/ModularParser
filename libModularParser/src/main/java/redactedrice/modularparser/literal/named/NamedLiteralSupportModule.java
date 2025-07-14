package redactedrice.modularparser.literal.named;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redactedrice.modularparser.core.BaseSupporter;

public class NamedLiteralSupportModule extends BaseSupporter<NamedLiteralParser> implements NamedLiteralSupporter {
    private final List<NamedLiteralParser> handlers = new ArrayList<>();

    public NamedLiteralSupportModule() {
        super("NamedLiteralSupportModule", NamedLiteralParser.class);
    }

    @Override
    public boolean isVariableDefined(String var) {
        for (NamedLiteralParser variableModule : handlers) {
            if (variableModule.isVariable(var)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> getAllVariables() {
        Map<String, Object> all = new HashMap<>();
        for (NamedLiteralParser variables : handlers) {
            all.putAll(variables.getVariables());
        }
        return all;
    }
}
