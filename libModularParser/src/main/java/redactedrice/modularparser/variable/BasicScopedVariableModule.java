package redactedrice.modularparser.variable;


import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.literal.LiteralSupporter;
import redactedrice.modularparser.scope.ScopeHandler;
import redactedrice.modularparser.scope.ScopedModule;

public class BasicScopedVariableModule extends ScopedModule
        implements LineHandler, VariableHandler {
    protected final boolean reassignmentAllowed;
    protected final String keyword;
    protected final Pattern matcher;

    protected LiteralSupporter literalHandler;

    public BasicScopedVariableModule(String moduleName, boolean reassignmentAllowed, String keyword,
            ScopeHandler scopeHandler) {
        super(moduleName, scopeHandler);
        this.keyword = keyword.toLowerCase();
        this.reservedWords.put(keyword, ReservedType.EXCLUSIVE);
        matcher = Pattern.compile("^\\s*(?:(" + this.keyword + ")\\s+)?(\\w+)\\s*=\\s*(.+)$");
        this.reassignmentAllowed = reassignmentAllowed;
    }

    @Override
    public void configure() {
        literalHandler = parser.getSupporterOfType(LiteralSupporter.class);
    }

    @Override
    public boolean scopedMatches(String scope, String logicalLine) {
        return matcher.matcher(logicalLine).find();
    }

    @Override
    public void scopedHandle(String scope, String logicalLine, String defaultScope) {
        Matcher m = matcher.matcher(logicalLine);
        if (!m.matches()) {
            return;
        }

        if (m.group(1) == null) {
            // reassignment
            if (scope.isEmpty()) { // scope was not specified
                scope = scopeHandler.getScope(m.group(2));
                if (scope == null) {
                    System.err.println(getName() + ": Attempted to reassign undefined " + keyword
                            + " " + m.group(2) + " with " + m.group(3));
                    return;
                }
            }

            if (!isValidName(m.group(2))) {
                System.err.println("Invalid variable name: " + m.group(2));
                return;
            }

            if (!scopeHandler.getOwner(Optional.of(scope), m.group(2)).isEmpty()) {
                if (reassignmentAllowed) {
                    addLiteral(scopeHandler, m.group(3), scope, m.group(2), false);
                } else {
                    System.err.println(getName() + ": Attempted to reassign " + keyword + " "
                            + m.group(2) + " in scope " + scope + " with " + m.group(3));
                }
            } else {
                System.err.println(getName() + ": Attempted to reassign non-existing " + keyword
                        + " " + m.group(2) + " in scope " + scope + " with " + m.group(3));
            }
        } else {
            // Assignment
            if (scope.isEmpty()) { // scope was not specified
                scope = defaultScope;
            }

            if (!isValidName(m.group(2))) {
                System.err.println("Invalid variable name: " + m.group(2));
                return;
            }

            if (scopeHandler.getOwner(Optional.of(scope), m.group(2)).isEmpty()) {
                addLiteral(scopeHandler, m.group(3), scope, m.group(2), true);
            } else {
                System.err.println(getName() + ": Attempted to redefine existing " + keyword + " "
                        + m.group(2) + " in scope " + scope + " with " + m.group(3));
            }
        }
    }

    private void addLiteral(ScopeHandler scope, String literal, String scopeName, String name,
            boolean assignment) {
        Object obj = literalHandler.evaluateLiteral(literal);
        if (obj != null) {
            if (scope.setData(scopeName, name, getName(), obj)) {
                System.out.println(getName() + ": " + (assignment ? "Added " : "Changed ") + keyword
                        + " " + name + " in scope " + scopeName + " with value: " + obj);
            }
        } else {
            throw new IllegalArgumentException("VariableHandler: For " + keyword + " " + name
                    + "\" + cannot parse value: " + literal);
        }
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        return Optional.ofNullable(scopeHandler.getData(Optional.empty(), literal, getName()));
    }

    @Override
    public boolean isVariable(String var) {
        return scopeHandler.getData(Optional.empty(), var, getName()) != null;
    }

    @Override
    public Map<String, Object> getVariables() {
        return Collections
                .unmodifiableMap(scopeHandler.getAllOwnedData(Optional.empty(), getName()));
    }
}
