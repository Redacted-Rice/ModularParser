package redactedrice.modularparser.basic;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import redactedrice.modularparser.ScopeHandler;
import redactedrice.modularparser.base.BaseModule;

public class BasicScopeModule extends BaseModule implements ScopeHandler {
    private class OwnedObject {
        public String owner;
        public Object obj;
    }

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
        for (Map<String, Object> scopedMap : scopedVals.values()) {
            try {
                scopedMap.put(module, dataClass.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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
    public Object getDataForScope(String scope, String module) {
        return scopedVals.get(scope).get(module);
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
            HashMap<String, Object> map = new HashMap<>();
            for (Entry<String, Class<?>> module : modules.entrySet()) {
                try {
                    map.put(module.getKey(),
                            module.getValue().getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            scopedVals.put(scope, map);
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
