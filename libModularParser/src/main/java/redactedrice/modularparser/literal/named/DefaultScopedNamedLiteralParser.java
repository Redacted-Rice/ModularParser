package redactedrice.modularparser.literal.named;


import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.literal.LiteralSupporter;
import redactedrice.modularparser.scope.BaseScopedParser;
import redactedrice.modularparser.scope.ScopeSupporter;

public class DefaultScopedNamedLiteralParser extends BaseScopedParser
        implements NamedLiteralParser {
    protected final boolean reassignmentAllowed;
    protected final String keyword;
    protected final Pattern matcher;

    protected LiteralSupporter literalHandler;

    public DefaultScopedNamedLiteralParser(String moduleName, boolean reassignmentAllowed,
            String keyword) {
        super(moduleName);
        this.keyword = keyword.toLowerCase();
        // this.reservedWords.put(keyword, ReservedType.EXCLUSIVE);
        matcher = Pattern.compile("^\\s*(?:(" + this.keyword + ")\\s+)?(\\w+)\\s*=\\s*(.+)$");
        this.reassignmentAllowed = reassignmentAllowed;
    }

    @Override
    public void configure() {
        super.configure();
        literalHandler = parser.getSupporterOfType(LiteralSupporter.class);
    }

    @Override
    public boolean tryParseScoped(String scope, String logicalLine, String defaultScope) {
        Matcher m = matcher.matcher(logicalLine);
        if (!m.matches()) {
            return false;
        }

        if (m.group(1) == null) {
            // reassignment
            if (scope.isEmpty()) { // scope was not specified
                scope = scopeSupporter.getScope(m.group(2));
                if (scope == null) {
                    System.err.println(getName() + ": Attempted to reassign undefined " + keyword
                            + " " + m.group(2) + " with " + m.group(3));
                    return true;
                }
            }

            if (!isValidName(m.group(2))) {
                System.err.println("Invalid variable name: " + m.group(2));
                return true;
            }

            if (!scopeSupporter.getOwner(Optional.of(scope), m.group(2)).isEmpty()) {
                if (reassignmentAllowed) {
                    addLiteral(scopeSupporter, m.group(3), scope, m.group(2), false);
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
                return true;
            }

            if (scopeSupporter.getOwner(Optional.of(scope), m.group(2)).isEmpty()) {
                addLiteral(scopeSupporter, m.group(3), scope, m.group(2), true);
            } else {
                System.err.println(getName() + ": Attempted to redefine existing " + keyword + " "
                        + m.group(2) + " in scope " + scope + " with " + m.group(3));
            }
        }
        return true;
    }

    private void addLiteral(ScopeSupporter scope, String literal, String scopeName, String name,
            boolean assignment) {
        Object obj = literalHandler.evaluateLiteral(literal);
        if (obj != null) {
            if (scope.setData(scopeName, name, this, obj)) {
                System.out.println(getName() + ": " + (assignment ? "Added " : "Changed ") + keyword
                        + " " + name + " in scope " + scopeName + " with value: " + obj);
            }
        } else {
            throw new IllegalArgumentException("VariableHandler: For " + keyword + " " + name
                    + "\" + cannot parse value: " + literal);
        }
    }

    @Override
    public Optional<Object> tryParseLiteral(String literal) {
        return Optional.ofNullable(scopeSupporter.getData(Optional.empty(), literal, this));
    }

    @Override
    public boolean isVariable(String var) {
        return scopeSupporter.getData(Optional.empty(), var, this) != null;
    }

    @Override
    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(scopeSupporter.getAllOwnedData(Optional.empty(), this));
    }
}
