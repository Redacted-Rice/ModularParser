package redactedrice.modularparser.basic;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

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
    public String matchesScope(String logicalLine) {
        if (logicalLine == null || logicalLine.isBlank()) {
            return "";
        }

        String[] words = logicalLine.trim().split("\\s+", 2);
        if (allowImplicit && !scopeOrder.contains(words[0])) {
            return logicalLine;
        } else {
            return scopeOrder.contains(words[0]) ? words[1] : "";
        }
    }

    @Override
    public String[] separateScope(String logicalLine) {
        String[] words = logicalLine.trim().split("\\s+", 2);
        if (scopeOrder.contains(words[0])) {
            return words;
        } else if (allowImplicit) {
            return new String[] { scopeOrder.peek(), logicalLine
            };
        }
        return new String[] {};
    }

    @Override
    public String getOwnerForScope(String scope, String name) {
        Map<String, OwnedObject> scopeMap = scopedVals.get(scope);
        if (scopeMap != null) {
            OwnedObject obj = scopeMap.get(name);
            if (obj != null) {
                return obj.owner();
            }
        }
        return "";
    }

    @Override
    public String[] getLowestOwnerAndScope(String name) {
        for (String scope : scopeOrder) {
            OwnedObject obj = scopedVals.get(scope).get(name);
            if (obj != null) {
                return new String[] { obj.owner, scope
                };
            }
        }
        return new String[] {};
    }

    @Override
    public Object getDataForScope(String scope, String name, String module) {
        Map<String, OwnedObject> scopeMap = scopedVals.get(scope);
        if (scopeMap != null) {
            OwnedObject obj = scopeMap.get(name);
            if (obj != null) {
                return obj.obj();
            }
        }
        return scopedVals.get(scope).get(module);
    }

    @Override
    public Object getDataInLowestScope(String name, String module) {
        for (String scope : scopeOrder) {
            Object obj = getDataForScope(scope, name, module);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    @Override
    public void setDataForScope(String scope, String name, String module, Object data) {
        scopedVals.get(scope).put(name, new OwnedObject(module, data));
    }

    @Override
    public String currentScope() {
        return scopeOrder.peek();
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
}
