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

        String[] split = scope.splitScope(logicalLine);
        if (split == null) {
            return false;
        }
        return matcher.matcher(split[1]).find();
    }

    private void addLiteral(ScopeHandler scope, String literal, String scopeName, String name,
            boolean assignment) {
        Object obj = parser.evaluateLiteral(literal);
        if (obj != null) {
            System.out.println(getName() + ": " + (assignment ? "Adding " : "Changing ") + keyword
                    + " " + name + " in scope " + scopeName + " with value: " + obj);
            scope.setData(scopeName, name, getName(), obj);
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

        String[] scopeLine = scope.splitScope(line);
        if (scopeLine.length <= 0) {
            return;
        }

        Matcher m = matcher.matcher(scopeLine[1]);
        if (!m.matches()) {
            return;
        }
        if (m.group(1) == null) {
            // reassignment
            if (scopeLine[0].isEmpty()) { // scope was not specified
            	scopeLine[0] = scope.getScope(m.group(2));
                if (scopeLine[0] == null) {
                    System.err.println(getName() + ": Attempted to reassign undefined " + keyword
                            + " " + m.group(2) + " with " + m.group(3));
                    return;
                }
            }
            
            if (!scope.getOwner(Optional.of(scopeLine[0]), m.group(2)).isEmpty()) {
                if (reassignmentAllowed) {
                    addLiteral(scope, m.group(3), scopeLine[0], m.group(2), false);
                } else {
                    System.err.println(getName() + ": Attempted to reassign " + keyword + " "
                            + m.group(2) + " in scope " + scopeLine[0] + " with " + m.group(3));
                }
            } else {
                System.err.println(getName() + ": Attempted to reassign non-existing " + keyword + " "
                        + m.group(2) + " in scope " + scopeLine[0] + " with " + m.group(3));
            }
        } else {
        	// Assignment
            if (scopeLine[0].isEmpty()) { // scope was not specified
            	scopeLine[0] = scope.currentScope();
            }

            if (scope.getOwner(Optional.of(scopeLine[0]), m.group(2)).isEmpty()) {
            	addLiteral(scope, m.group(3), scopeLine[0], m.group(2), true);
            } else {
                System.err.println(getName() + ": Attempted to redefine existing " + keyword + " "
                        + m.group(2) + " in scope " + scopeLine[0] + " with " + m.group(3));
            }
        }
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(scope.getData(Optional.empty(), literal, getName()));
    }

    @Override
    public boolean isVariable(String var) {
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return false;
        }
        return scope.getData(Optional.empty(), var, getName()) != null;
    }
}
