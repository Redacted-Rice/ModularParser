package redactedrice.modularparser.variable;

import java.util.Map;

public interface VariableSupporter {
	public void addVariableParser(VariableHandler parser);
    public boolean isVariableDefined(String var);
    public Map<String, Object> getAllVariables();
}
