package redactedrice.modularparser.scope;


import java.util.Map;

public interface ScopeSupporter {
    public void addScopeHandler(ScopeHandler parser);

    public boolean isVariableDefined(String var);

    public Map<String, Object> getAllVariables();
}
