package redactedrice.modularparser.literal.named;

import java.util.Map;

public interface NamedLiteralSupporter {
	public void addVariableParser(NamedLiteralHandler parser);
    public boolean isVariableDefined(String var);
    public Map<String, Object> getAllVariables();
}
