package redactedrice.modularparser;


public interface ScopeHandler {
    void addScopedModule(String module, Class<?> dataClass);

    boolean handlesModule(String module);

    String matchesScope(String logicalLine);

    String[] separateScope(String logicalLine);

    Object getDataForScope(String scope, String module);

    String currentScope();

    void pushScope(String scope);

    void popScope();

    void removeScope(String scope);
}
