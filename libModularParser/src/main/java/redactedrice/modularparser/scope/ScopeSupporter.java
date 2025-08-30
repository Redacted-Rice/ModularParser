package redactedrice.modularparser.scope;


import java.util.Map;
import java.util.Set;

import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.core.Supporter;

public interface ScopeSupporter extends Supporter {
    boolean pushScope(String scope);

    boolean popScope();

    boolean removeScope(String scope);

    String currentScope();

    // Splits the scope from the logical line if present or implicit is allowed.
    // Otherwise returns null
    String[] splitScope(String logicalLine);

    default boolean doesOwn(Module module, String scope, String name) {
        return getOwner(scope, name).equals(module.getName());
    }

    String getOwner(String scope, String name);

    String getNarrowestScope(String name);

    Response<Object> getData(String scope, String name, Module owner);

    Set<String> getAllOwnedNames(String scope, Module owner);

    Map<String, Object> getAllOwnedData(String scope, Module owner);

    boolean setData(String scope, String name, Module owner, Object data);
}
