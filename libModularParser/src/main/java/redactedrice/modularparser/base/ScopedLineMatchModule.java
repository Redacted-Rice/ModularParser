package redactedrice.modularparser.base;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.Scoped;

public abstract class ScopedLineMatchModule<T> extends ReservedWordModule
        implements LineHandler, Scoped {
    protected final Map<String, Map<String, T>> scopedVals = new HashMap<>();
    protected final Deque<String> scopeOrder = new ArrayDeque<>();
    protected final boolean allowImplicit;

    public ScopedLineMatchModule(String name, boolean allowImplicit) {
        super(name);
        this.allowImplicit = allowImplicit;
    }

    public abstract boolean matches(String logicalLine, Map<String, T> scopedMap);

    @Override
    public boolean matches(String logicalLine) {
        if (logicalLine == null || logicalLine.isBlank()) {
            return false;
        }

        String[] words = logicalLine.trim().split("\\s+", 2);
        if (allowImplicit && !scopeOrder.contains(words[0])) {
            return matches(logicalLine, scopedVals.get(scopeOrder.peek()));
        } else {
            return matches(words[1], scopedVals.get(words[0]));
        }
    }

    public abstract void handle(String logicalLine, Map<String, T> scopedMap);

    @Override
    public void handle(String logicalLine) {
        String[] words = logicalLine.trim().split("\\s+", 2);
        if (allowImplicit && !scopeOrder.contains(words[0])) {
            handle(logicalLine, scopedVals.get(scopeOrder.peek()));
        } else {
            handle(words[1], scopedVals.get(words[0]));
        }
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
        scopeOrder.removeFirst();
    }

    @Override
    public void removeScope(String scope) {
        if (scopedVals.containsKey(scope)) {
            scopeOrder.remove(scope);
        } else {
            System.err.println("Attempting to pop undefined scope: " + scope);
        }
    }
}
