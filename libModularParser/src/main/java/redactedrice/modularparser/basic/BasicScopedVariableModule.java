package redactedrice.modularparser.basic;


import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.VariableHandler;
import redactedrice.modularparser.WordReserver;

public class BasicScopedVariableModule extends BaseModule
        implements LineHandler, VariableHandler, WordReserver {
    public enum ImplicitType {
        NONE, KEYWORD, ALL
    }

    protected final String keyword;
    protected final Map<String, ReservedType> reservedWords = new HashMap<>();
    protected final Map<String, Map<String, Object>> scopedVariables = new HashMap<>();
    Deque<String> scopeOrder = new ArrayDeque<>();
    protected final Pattern assignDef;
    protected final Pattern reassignmentDef;
    protected final boolean reassignmentAllowed;

    public BasicScopedVariableModule(String moduleName, boolean implicitAllowed,
            boolean reassignmentAllowed, String keyword, String... qualifiedScopes) {
        super(moduleName);

        this.keyword = keyword.toLowerCase();
        reservedWords.put(this.keyword, ReservedType.EXCLUSIVE);

        for (String scope : qualifiedScopes) {
            reservedWords.put(scope.toLowerCase(), ReservedType.SHAREABLE);
            scopedVariables.put(scope, new HashMap<>());
            scopeOrder.addFirst(scope.toLowerCase());
        }

        if (implicitAllowed) {
            assignDef = Pattern.compile("^\\s*(?:(?!" + this.keyword + "\\b)(\\w+)\\s+)?(?:"
                    + this.keyword + "\\s+)?(\\w+)\\s*=\\s*(.+)$");
        } else {
            assignDef = Pattern
                    .compile("^\\s*(?:(\\w+)\\s+)?" + this.keyword + "\\s+(\\w+)\\s*=\\s*(.+)$");
        }

        this.reassignmentAllowed = reassignmentAllowed;
        if (reassignmentAllowed) {
            reassignmentDef = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.+)$");
        } else {
            reassignmentDef = Pattern.compile("(?!)"); // never matches
        }

    }

    @Override
    public boolean matches(String logicalLine) {
        if (logicalLine == null || logicalLine.isBlank()) {
            return false;
        }

        Matcher assign = assignDef.matcher(logicalLine);
        if (assign.matches()) {
            if (assign.group(1) == null) {
                return true; // will use default scope
            } else {
                return scopedVariables.containsKey(assign.group(1));
            }
        }

        return reassignmentAllowed && reassignmentDef.matcher(logicalLine).matches();
    }

    private String findReassignScope(Matcher reassign) {
        // if its reassign, ensure we have an in scope variable to use
        for (String scope : scopeOrder) {
            if (scopedVariables.get(scope).containsKey(reassign.group(1))) {
                return scope;
            }
        }
        System.err.println(
                "Attempting to reassign a value not defined in any scope: " + reassign.group(1));
        return "";
    }

    private boolean checkAssignScope(String assignScope, Matcher assign) {
        // if its reassign, ensure we have an in scope variable to use
        for (String scope : scopeOrder) {
            if (scopedVariables.get(scope).containsKey(assign.group(2))) {
                if (assignScope == scope) {
                    System.err.println("Attempting to redefine existing value: " + assign.group(2));
                    return false;
                } else {
                    System.err.println("Definition for val " + assign.group(2) + " in scope "
                            + scope + " will be obscured with value in scope " + assignScope);
                }
            }
        }
        return true;
    }

    @Override
    public void handle(String line) {
        Matcher assign = assignDef.matcher(line);
        // If its assign and not specified, use the most recent scope
        String foundScope = assign.matches() && assign.group(1) != null ? assign.group(1)
                : scopeOrder.peek();

        Matcher reassign = null;
        if (reassignmentAllowed) {
            reassign = reassignmentDef.matcher(line);
            if (!reassign.matches() && !assign.matches()) {
                return;
            } else if (!assign.matches()) {
                foundScope = findReassignScope(reassign);
                if (!foundScope.isEmpty()) {
                    return;
                }
            } else if (!reassign.matches()) {
                if (!checkAssignScope(foundScope, assign)) {
                    return;
                }
            }
        } else if (!assign.matches()) {
            return;
        }

        // At this point we either have assign or reassign. If we have both
        // it doesn't matter which we use
        String varName = assign.matches() ? assign.group(2) : reassign.group(1);
        String literal = assign.matches() ? assign.group(3) : reassign.group(2);

        Object obj = parser.evaluateLiteral(literal);
        if (obj != null) {
            scopedVariables.get(foundScope).put(varName, obj);
            System.out.println(getName() + ": Added " + keyword + " " + varName + " in scope "
                    + foundScope + " with value: " + obj);
        } else {
            throw new IllegalArgumentException("VariableHandler: For variable \"" + varName
                    + "\" + cannot parse value: " + literal);
        }
    }

    @Override
    public boolean isReservedWord(String word, Optional<ReservedType> type) {
        if (type.isEmpty()) {
            return reservedWords.containsKey(word) || isVariable(word);
        } else if (type.get() == ReservedType.SHAREABLE) {
            return reservedWords.get(word) == ReservedType.SHAREABLE || isVariable(word);
        } else {
            return reservedWords.get(word) == type.get();
        }
    }

    @Override
    public Map<String, ReservedType> getAllReservedWords() {
        Map<String, ReservedType> all = new HashMap<>(reservedWords);
        scopedVariables.values().stream().forEach(variables -> variables.keySet().stream()
                .forEach(var -> all.put(var, ReservedType.SHAREABLE)));
        return Collections.unmodifiableMap(all);
    }

    @Override
    public Set<String> getReservedWords(ReservedType type) {
        Set<String> all = new HashSet<>(reservedWords.entrySet().stream()
                .filter(entry -> entry.getValue() == type).map(Map.Entry::getKey).toList());
        if (type == ReservedType.SHAREABLE) {
            scopedVariables.values().stream().forEach(variables -> all.addAll(variables.keySet()));
        }
        return Collections.unmodifiableSet(all);
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        for (String scope : scopeOrder) {
            if (scopedVariables.get(scope).containsKey(literal)) {
                return Optional.ofNullable(scopedVariables.get(scope).get(literal));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isVariable(String var) {
        for (String scope : scopeOrder) {
            if (scopedVariables.get(scope).containsKey(var)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> getVariables() {
        Map<String, Object> all = new HashMap<>();
        for (String scope : scopeOrder) {
            scopedVariables.get(scope).entrySet().stream()
                    .forEach(entry -> all.putIfAbsent(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableMap(all);
    }
}
