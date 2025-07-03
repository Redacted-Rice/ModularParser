package redactedrice.modularparser.basic;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.ScopeHandler;
import redactedrice.modularparser.VariableHandler;
import redactedrice.modularparser.WordReserver.ReservedType;
import redactedrice.modularparser.base.ReservedWordModule;

public class BasicScopedVariableModule extends ReservedWordModule
        implements LineHandler, VariableHandler {
    protected final boolean reassignmentAllowed;
    protected final String keyword;
    protected final Pattern matcher;

    public BasicScopedVariableModule(String moduleName, boolean reassignmentAllowed,
            String keyword) {
        super(moduleName);
        this.keyword = keyword.toLowerCase();
        this.reservedWords.put(keyword, ReservedType.EXCLUSIVE);
        matcher = Pattern.compile("^\\s*(?:(" + this.keyword + ")\\s+)?(?!" + this.keyword
                + "\\b)(\\w+)\\s*=\\s*(.+)$");
        this.reassignmentAllowed = reassignmentAllowed;
    }

    public Class<?> dataClass() {
        return HashMap.class;
    }

    @Override
    public boolean matches(String logicalLine) {
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return false;
        }

        String scopeless = scope.matchesScope(logicalLine);
        if (scopeless.isEmpty()) {
            return false;
        }

        return matcher.matcher(logicalLine).find();
    }

    @Override
    public void handle(String line) {
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return;
        }

        String[] scopeLine = scope.separateScope(line);
        if (scopeLine.length <= 0) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) scope.getDataForScope(scopeLine[0],
                getName());

        Matcher m = matcher.matcher(scopeLine[1]);

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
