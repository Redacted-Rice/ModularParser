package redactedrice.modularparser.scope;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ScopeHandler {	
    void addScopedModule(String module);

    boolean handlesModule(String module);

    void pushScope(String scope);

    void popScope();

    void removeScope(String scope);

    String currentScope();

    // Splits the scope from the logical line if present or implicit is allowed. 
    // Otherwise returns null
    String[] splitScope(String logicalLine);

    default boolean doesOwn(String module, Optional<String> scope, String name) {
        return getOwner(scope, name) == module;
    }
    
    String getOwner(Optional<String> scope, String name);

    String getScope(String name);

    Object getData(Optional<String> scope, String name, String owner);
    
    Set<String> getAllOwnedNames(Optional<String> scope, String owner);
    
    Map<String, Object> getAllOwnedData(Optional<String> scope, String owner);

    boolean setData(String scope, String name, String owner, Object data);
}
