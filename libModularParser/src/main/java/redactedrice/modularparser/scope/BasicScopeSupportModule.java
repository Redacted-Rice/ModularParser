package redactedrice.modularparser.scope;


import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.core.BaseSupporter;
import redactedrice.modularparser.lineparser.LineParser;

public class BasicScopeSupportModule extends BaseSupporter<ScopedParser> implements ScopeSupporter, LineParser {
    private record OwnedObject(String owner, Object obj) {}

    // Scope -> var -> owner + data
    protected final Map<String, Map<String, OwnedObject>> scopedVals = new HashMap<>();
    // Owner -> scope -> vars
    protected final Map<String, Map<String, Set<String>>> ownerMap = new HashMap<>();
    protected final Deque<String> scopeOrder = new ArrayDeque<>();
    protected final boolean allowImplicit;

    public BasicScopeSupportModule(String name, boolean allowImplicit) {
        super(name, ScopedParser.class);
        this.allowImplicit = allowImplicit;
    }

    @Override
    public boolean tryParseLine(String logicalLine) {
        String[] split = splitScope(logicalLine);
        if (split == null || split.length <= 0) {
            return false;
        }
        
        for (ScopedParser scoped : submodules) {
        	if (scoped.tryParseScoped(split[0], split[1], currentScope())) {
        		return true;
        	}
        }
        return false;
	}

    @Override
    public void pushScope(String scope) {
        if (scopedVals.containsKey(scope)) {
            System.err.println("Adding already exising scope, moving to last defined: " + scope);
            scopeOrder.remove(scope);
        } else {
            scopedVals.put(scope, new HashMap<>());
            ownerMap.values().stream().forEach(scopeMap -> scopeMap.put(scope, new HashSet<>()));
        }
        scopeOrder.addFirst(scope);
    }

    @Override
    public void popScope() {
        if (scopeOrder.isEmpty()) {
            System.err.println("No scope to pop!");
        }
        removeScope(scopeOrder.peek());
    }

    @Override
    public void removeScope(String scope) {
        if (scopedVals.containsKey(scope)) {
            scopeOrder.remove(scope);
            scopedVals.remove(scope);
            for (Map<?, ?> scopeMap : ownerMap.values()) {
                scopeMap.remove(scope);
            }
        } else {
            System.err.println("Attempting to remove undefined scope: " + scope);
        }
    }

    @Override
    public String currentScope() {
        return scopeOrder.peek();
    }

    @Override
    public String[] splitScope(String logicalLine) {
        String[] words = logicalLine.trim().split("\\s+", 2);
        if (scopeOrder.contains(words[0])) {
            return words;
        } else if (allowImplicit) {
            return new String[] { "", logicalLine
            };
        }
        return null;
    }
    
    private OwnedObject getDataForScopeOrLowestScope(Optional<String> scope, String name) {
        if (!scope.isEmpty() && !scope.get().isEmpty()) {
            Map<String, OwnedObject> scopeMap = scopedVals.get(scope.get());
            if (scopeMap != null) {
                OwnedObject obj = scopeMap.get(name);
                if (obj != null) {
                    return obj;
                }
            }
            return null;
        }
        for (String scopeCheck : scopeOrder) {
            OwnedObject obj = scopedVals.get(scopeCheck).get(name);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    @Override
    public String getOwner(Optional<String> scope, String name) {
        OwnedObject obj = getDataForScopeOrLowestScope(scope, name);
        if (obj != null) {
            return obj.owner();
        }
        return "";
    }

    @Override
    public String getScope(String name) {
        for (String scopeCheck : scopeOrder) {
            OwnedObject obj = scopedVals.get(scopeCheck).get(name);
            if (obj != null) {
                return scopeCheck;
            }
        }
        return "";
    }

    @Override
    public Object getData(Optional<String> scope, String name, String module) {
        OwnedObject obj = getDataForScopeOrLowestScope(scope, name);
        if (obj != null) {
            return obj.obj();
        }  
        return null;
    }

    @Override
    public Set<String> getAllOwnedNames(Optional<String> scope, String owner) {
        Map<String, Set<String>> owned = ownerMap.get(owner);
        if (owned == null) {
            return Collections.emptySet();
        }
        Set<String> vars;
        if (!scope.isEmpty() && !scope.get().isEmpty()) {
            vars = owned.get(scope.get());
        } else {
            vars = new HashSet<>();
            owned.values().forEach(scopeVars -> vars.addAll(scopeVars));
        }
        return vars;
    }
    
    @Override
    public Map<String, Object> getAllOwnedData(Optional<String> scope, String owner) {
        Set<String> names = getAllOwnedNames(scope, owner);
        Map<String, Object> data = new HashMap<>();
        names.stream().forEach(name -> data.put(name, getDataForScopeOrLowestScope(scope, name).obj()));
        return data;
    }

    @Override
    public boolean setData(String scope, String name, String owner, Object data) {
        Map<String, OwnedObject> scopeMap = scopedVals.get(scope);
        if (scopeMap == null) {
            return false;
        }
        
        OwnedObject obj = scopeMap.get(name);
        if (obj != null) {
            if (!obj.owner().equals(owner)) {
                System.err.println(owner + " attempted to set value for " + name + " in scope " + scope + " that is owned by " + obj.owner());
                return false;
            }
        }
        scopeMap.put(name, new OwnedObject(owner, data));
        ownerMap.get(owner).get(scope).add(name);
        return true;
    }
}
