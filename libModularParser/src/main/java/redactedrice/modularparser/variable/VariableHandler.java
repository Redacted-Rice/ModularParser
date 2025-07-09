package redactedrice.modularparser.variable;


import java.util.Map;

import redactedrice.modularparser.literal.LiteralHandler;

public interface VariableHandler extends LiteralHandler {
    boolean isVariable(String var);

    Map<String, Object> getVariables();
}
