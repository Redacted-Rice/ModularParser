package redactedrice.modularparser;

import java.util.Map;

public interface VariableHandler extends LiteralHandler {
    boolean isVariable(String var);

	Map<String, Object> getVariables();
}
