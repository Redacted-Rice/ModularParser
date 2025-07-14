package redactedrice.modularparser.literal.named;

import java.util.Map;

import redactedrice.modularparser.core.Supporter;

public interface NamedLiteralSupporter extends Supporter {
    public boolean isVariableDefined(String var);
    public Map<String, Object> getAllVariables();
}
