package redactedrice.modularparser.basic;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import redactedrice.modularparser.ScopeHandler;

public class BasicScopeModule extends BaseModule implements ScopeHandler {
    private record OwnedObject(String owner, Object obj) {}

    protected final Map<String, Class<?>> modules = new HashMap<>();
    protected final Map<String, Map<String, OwnedObject>> scopedVals = new HashMap<>();
    protected final Deque<String> scopeOrder = new ArrayDeque<>();
    protected final boolean allowImplicit;

    public BasicScopeModule(String name, boolean allowImplicit) {
        super(name);
        this.allowImplicit = allowImplicit;
    }

    @Override
    public void addScopedModule(String module, Class<?> dataClass) {
        modules.put(module, dataClass);
    }

    @Override
    public boolean handlesModule(String module) {
        return modules.containsKey(module);
    }

    @Override
    public void pushScope(String scope) {
        if (scopedVals.containsKey(scope)) {
            System.err.println("Adding already exising scope, moving to last defined: " + scope);
            scopeOrder.remove(scope);
        } else {
            scopedVals.put(scope, new HashMap<>());
        }
        scopeOrder.addFirst(scope);
    }

    @Override
    public void popScope() {
        if (scopeOrder.isEmpty()) {
            System.err.println("No scope to pop!");
        }
        String scope = scopeOrder.removeFirst();
        scopedVals.remove(scope);
    }

    @Override
    public void removeScope(String scope) {
        if (scopedVals.containsKey(scope)) {
            scopeOrder.remove(scope);
            scopedVals.remove(scope);
        } else {
            System.err.println("Attempting to pop undefined scope: " + scope);
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
        return true;
    }
}
