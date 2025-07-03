package redactedrice.modularparser;


public interface ScopeHandler {
    void addScopedModule(String module, Class<?> dataClass);

    boolean handlesModule(String module);

    String matchesScope(String logicalLine);

    String[] separateScope(String logicalLine);

    String getOwnerForScope(String scope, String name);

    String[] getLowestOwnerAndScope(String name);

    default boolean isOwnedForLowestScope(String scope, String name, String module) {
        return getOwnerForScope(scope, name) == module;
    }

    Object getDataForScope(String scope, String name, String module);

    Object getDataInLowestScope(String name, String module);

    void setDataForScope(String scope, String name, String module, Object data);

    String currentScope();

    void pushScope(String scope);

    void popScope();

    void removeScope(String scope);
}
