package redactedrice.modularparser;


import java.util.Map;

import redactedrice.modularparser.literals.LiteralParser;

public interface VariableHandler extends LiteralParser {
    boolean isVariable(String var);

    Map<String, Object> getVariables();
}
