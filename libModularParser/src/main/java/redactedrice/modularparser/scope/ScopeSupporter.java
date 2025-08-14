package redactedrice.modularparser.scope;


import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.Supporter;

public interface ScopeSupporter extends Supporter {
    boolean pushScope(String scope);

    boolean popScope();

    boolean removeScope(String scope);

    String currentScope();

    // Splits the scope from the logical line if present or implicit is allowed.
    // Otherwise returns null
    String[] splitScope(String logicalLine);

    default boolean doesOwn(Module module, Optional<String> scope, String name) {
        return getOwner(scope, name) == module.getName();
    }

    String getOwner(Optional<String> scope, String name);

    String getNarrowestScope(String name);

    Object getData(Optional<String> scope, String name, Module owner);

    Set<String> getAllOwnedNames(Optional<String> scope, Module owner);

    Map<String, Object> getAllOwnedData(Optional<String> scope, Module owner);

    boolean setData(String scope, String name, Module owner, Object data);
}
