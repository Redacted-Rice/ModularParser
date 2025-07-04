package redactedrice.modularparser.basic;


import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.ScopeHandler;
import redactedrice.modularparser.VariableHandler;

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
        matcher = Pattern.compile("^\\s*(?:(" + this.keyword + ")\\s+)?(\\w+)\\s*=\\s*(.+)$");
        this.reassignmentAllowed = reassignmentAllowed;
    }

    public Class<?> getDataClass() {
        return HashMap.class;
    }

    @Override
    public boolean matches(String logicalLine) {
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return false;
        }

        logicalLine = scope.matchesScope(logicalLine);
        if (logicalLine.isEmpty()) {
            return false;
        }

        return matcher.matcher(logicalLine).find();
    }

    private void addLiteral(ScopeHandler scope, String literal, String scopeName, String name,
            boolean assignment) {
        Object obj = parser.evaluateLiteral(literal);
        if (obj != null) {
            scope.setDataForScope(scopeName, name, getName(), obj);
            System.out.println(getName() + ": " + (assignment ? "Added " : "Changed ") + keyword
                    + " " + name + " in scope " + scopeName + " with value: " + obj);
        } else {
            throw new IllegalArgumentException("VariableHandler: For " + keyword + " " + name
                    + "\" + cannot parse value: " + literal);
        }
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

        Matcher m = matcher.matcher(scopeLine[1]);
        if (!m.matches()) {
            return;
        }
        if (m.group(1) == null) {
            // reassignment
            String owner;
            if (scopeLine[1].equals(line)) { // scope was not specified
                String[] ownerScope = scope.getLowestOwnerAndScope(m.group(2));
                if (ownerScope.length <= 0) {
                    System.err.println(getName() + ": Attempted to reassign non-exsiting " + keyword
                            + " " + m.group(2) + " with " + m.group(3));
                    return;
                }
                owner = ownerScope[0];
                scopeLine[0] = ownerScope[1];
            } else { // scope specified
                owner = scope.getOwnerForScope(scopeLine[0], m.group(2));
            }
            if (owner == getName()) {
                if (reassignmentAllowed) {
                    addLiteral(scope, m.group(3), scopeLine[0], m.group(2), false);
                } else {
                    System.err.println(getName() + ": Attempted to reassign " + keyword + " "
                            + m.group(2) + " in scope " + scopeLine[0] + " with " + m.group(3));
                }
            } else {
                System.err.println(getName() + ": Attempted to reassign non-existing/unowned " + keyword + " "
                        + m.group(2) + " in scope " + scopeLine[0] + " with " + m.group(3));
            }
        } else {
            // Assignment
            String owner = scope.getOwnerForScope(scopeLine[0], m.group(2));
            if (!owner.isEmpty()) {
                System.err.println("Value " + m.group(2) + " is already defined for scope "
                        + scopeLine[0] + " by " + owner);
            } else {
                addLiteral(scope, m.group(3), scopeLine[0], m.group(2), true);
            }
        }
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(scope.getDataInLowestScope(literal, getName()));
    }

    @Override
    public boolean isVariable(String var) {
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return false;
        }
        return scope.getDataInLowestScope(var, getName()) != null;
    }
}
