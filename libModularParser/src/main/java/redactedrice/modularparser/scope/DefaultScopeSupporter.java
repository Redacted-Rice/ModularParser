package redactedrice.modularparser.scope;


import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineparser.LineParser;
import redactedrice.modularparser.reserved.WordReserver;

public class DefaultScopeSupporter extends BaseModule
        implements ScopeSupporter, LineParser, WordReserver {
    protected final List<ScopedParser> parsers = new LinkedList<>();
    // Scope -> var -> owner + data
    protected final Map<String, Map<String, OwnedObject>> scopedVals = new HashMap<>();
    // Owner -> scope -> vars
    protected final Map<String, Map<String, Set<String>>> ownerMap = new HashMap<>();
    protected final Deque<String> scopeOrder = new ArrayDeque<>();
    protected final boolean allowImplicit;

    public DefaultScopeSupporter(boolean allowImplicit) {
        super(DefaultScopeSupporter.class.getSimpleName());
        this.allowImplicit = allowImplicit;
    }

    @Override
    public void handleModule(Module module) {
        if (module instanceof ScopedParser parser) {
            parsers.add(parser);
            Map<String, Set<String>> ownerScopeMap = new HashMap<>();
            scopeOrder.stream().forEach(scope -> ownerScopeMap.put(scope, new HashSet<>()));
            ownerMap.put(module.getName(), ownerScopeMap);
        }
    }

    @Override
    public boolean tryParseLine(String logicalLine) {
        String[] split = splitScope(logicalLine);
        if (split == null || split.length <= 0) {
            return false;
        }

        for (ScopedParser scoped : parsers) {
            if (scoped.tryParseScoped(split[0], split[1], currentScope())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pushScope(String scope) {
        if (scopedVals.containsKey(scope)) {
            log(LogLevel.ERROR, "Adding already exising scope, moving to last defined: %s", scope);
            return false;
        } else {
            scopedVals.put(scope, new HashMap<>());
            ownerMap.values().stream().forEach(scopeMap -> scopeMap.put(scope, new HashSet<>()));
        }
        scopeOrder.addFirst(scope);
        return true;
    }

    @Override
    public boolean popScope() {
        if (scopeOrder.isEmpty()) {
            log(LogLevel.ERROR, "No scope to pop");
            return false;
        }
        removeScope(scopeOrder.peek());
        return true;
    }

    @Override
    public boolean removeScope(String scope) {
        if (scopedVals.containsKey(scope)) {
            scopeOrder.remove(scope);
            scopedVals.remove(scope);
            for (Map<?, ?> scopeMap : ownerMap.values()) {
                scopeMap.remove(scope);
            }
        } else {
            log(LogLevel.ERROR, "Attempting to remove undefined scope: %s", scope);
            return false;
        }
        return true;
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
            return new String[] {"", logicalLine};
        }
        return null;
    }

    protected OwnedObject getDataForScopeOrNarrowestScope(String scope, String name) {
        if (scope != null && !scope.isEmpty()) {
            Map<String, OwnedObject> scopeMap = scopedVals.get(scope);
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
    public Response<String> getOwner(String scope, String name) {
        OwnedObject obj = getDataForScopeOrNarrowestScope(scope, name);
        if (obj != null) {
            return Response.is(obj.owner());
        }
        return Response.notHandled();
    }

    @Override
    public Response<String> getNarrowestScope(String name) {
        for (String scopeCheck : scopeOrder) {
            OwnedObject obj = scopedVals.get(scopeCheck).get(name);
            if (obj != null) {
                return Response.is(scopeCheck);
            }
        }
        return Response.notHandled();
    }

    @Override
    public Response<Object> getData(String scope, String name, Module owner) {
        OwnedObject obj = getDataForScopeOrNarrowestScope(scope, name);
        if (obj != null && obj.owner().equals(owner.getName())) {
            return Response.is(obj.obj());
        }
        return Response.notHandled();
    }

    @Override
    public Set<String> getAllOwnedNames(String scope, Module owner) {
        Map<String, Set<String>> owned = ownerMap.get(owner.getName());
        if (owned == null) {
            return Collections.emptySet();
        }
        Set<String> vars;
        if (scope != null && !scope.isEmpty()) {
            vars = owned.get(scope);
        } else {
            vars = new HashSet<>();
            owned.values().forEach(vars::addAll);
        }
        return vars;
    }

    @Override
    public Map<String, Object> getAllOwnedData(String scope, Module owner) {
        Set<String> names = getAllOwnedNames(scope, owner);
        Map<String, Object> data = new HashMap<>();
        names.stream().forEach(
                name -> data.put(name, getDataForScopeOrNarrowestScope(scope, name).obj()));
        return data;
    }

    @Override
    public boolean setData(String scope, String name, Module owner, Object data) {
        Map<String, OwnedObject> scopeMap = scopedVals.get(scope);
        if (scopeMap == null) {
            return false;
        }

        OwnedObject obj = scopeMap.get(name);
        if (obj != null && !obj.owner().equals(owner.getName())) {
            log(LogLevel.ERROR, "%s attempted to set value for %s in scope %s that is owned by %s",
                    owner.getName(), name, scope, obj.owner());
            return false;
        }
        scopeMap.put(name, new OwnedObject(owner.getName(), data));
        ownerMap.get(owner.getName()).get(scope).add(name);
        return true;
    }

    @Override
    public boolean isReservedWord(String word) {
        for (Map<String, OwnedObject> valMap : scopedVals.values()) {
            if (valMap.containsKey(word)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getReservedWords() {
        Set<String> forAllScopes = new HashSet<>();
        scopedVals.values().stream().forEach(valMap -> forAllScopes.addAll(valMap.keySet()));
        return forAllScopes;
    }
}
