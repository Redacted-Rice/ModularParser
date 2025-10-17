package redactedrice.modularparser.scope;


import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.LiteralParser;
import redactedrice.modularparser.literal.LiteralSupporter;

public class DefaultScopedVarConstParser extends BaseScopedKeywordParser implements LiteralParser {
    protected final boolean reassignmentAllowed;
    protected final Pattern matcher;

    protected LiteralSupporter literalSupporter;

    public DefaultScopedVarConstParser(String moduleName, boolean reassignmentAllowed,
            String keyword) {
        super(moduleName, keyword);
        matcher = Pattern.compile("^\\s*(?:(" + this.getKeyword() + ")\\s+)?(\\w+)\\s*=\\s*(.+)$");
        this.reassignmentAllowed = reassignmentAllowed;
    }

    @Override
    public void setModuleRefs() {
        super.setModuleRefs();
        literalSupporter = parser.getSupporterOfType(LiteralSupporter.class);
    }

    @Override
    public boolean tryParseScoped(String scope, String logicalLine, String defaultScope) {
        Matcher m = matcher.matcher(logicalLine);
        if (!m.matches()) {
            return literalSupporter.evaluateLiteral(logicalLine).wasHandled();
        }

        if (m.group(1) == null && !reassignmentAllowed) {
            log(LogLevel.DEBUG, "See reassignment but its not supported by this module: %s",
                    logicalLine);
            return false;
        }

        final String key = m.group(2);
        if (!isValidName(key)) {
            log(LogLevel.ERROR, "Invalid variable name: %s", key);
            return true;
        }

        if (m.group(1) == null) {
            handleReassignment(scope, key, m.group(3));
        } else {
            handleAssignment(scope, defaultScope, key, m.group(3));
        }
        return true;
    }

    protected boolean handleReassignment(String scope, String key, String value) {
        // reassignment
        if (scope == null || scope.isEmpty()) { // scope was not specified
            Response<String> response = scopeSupporter.getNarrowestScope(key);
            if (!response.wasValueReturned()) {
                log(LogLevel.ERROR,
                        "Failed to find scope for %s %s with %s. Most likely var was not defined",
                        getKeyword(), key, value);
                return false;
            }
            scope = response.getValue();
        }

        // Check for collisions with reserved words outside scope supporter
        if (!ensureWordAvailableOrOwned(key)) {
            return false;
        }

        Response<String> owner = scopeSupporter.getOwner(scope, key);
        if (!owner.wasValueReturned()) {
            log(LogLevel.ERROR, "Attempted to reassign non-existing %s %s in scope %s with %s",
                    getKeyword(), key, scope, value);
        } else if (!owner.getValue().equals(getName())) {
            log(LogLevel.ERROR,
                    "Attempted to reassign value of %s in scope %s with %s which is owned by module %s",
                    key, scope, value, owner);
        } else {
            addLiteral(value, scope, key, false);
        }
        return true;
    }

    protected boolean handleAssignment(String scope, String defaultScope, String key,
            String value) {
        if (scope == null || scope.isEmpty()) { // scope was not specified
            scope = defaultScope;
        }

        // Check for collisions with reserved words outside scope supporter
        if (!ensureWordAvailableOrOwned(key)) {
            return false;
        }

        Response<String> owner = scopeSupporter.getOwner(scope, key);
        if (owner.wasValueReturned()) {
            log(LogLevel.ERROR,
                    "Attempted to redefine existing value %s in scope %s with %s owned by %s", key,
                    scope, value, owner.getValue());
        } else if (owner.wasError()) {
            log(LogLevel.ERROR, "Encountered error while assigning %s %s in scope %s with %s: %s",
                    getKeyword(), key, scope, value, owner.getError());
        } else {
            addLiteral(value, scope, key, true);
        }
        return true;
    }

    protected void addLiteral(String literal, String scopeName, String name, boolean assignment) {
        Response<Object> obj = literalSupporter.evaluateLiteral(literal);
        if (obj.wasValueReturned()) {
            if (scopeSupporter.setData(scopeName, name, this, obj.getValue())) {
                log(LogLevel.DEBUG, "%s %s %s in scope %s with %s",
                        (assignment ? "Added " : "Changed "), getKeyword(), name, scopeName,
                        obj.getValue());
            }
        } else {
            log(LogLevel.ERROR, "For %s %s cannot parse value \"%s\". %s", getKeyword(), name,
                    literal,
                    obj.wasError() ? "Error: " + obj.getError() : "literal was not handled");
        }
    }

    @Override
    public Response<Object> tryParseLiteral(String literal) {
        return scopeSupporter.getData(null, literal, this);
    }

    public boolean setVariable(String scopeName, String name, Object val) {
        return scopeSupporter.setData(scopeName, name, this, val);
    }

    public boolean isVariable(String name) {
        return scopeSupporter.getData(null, name, this).wasHandled();
    }

    public Response<Object> getVariableValue(String name) {
        return scopeSupporter.getData(null, name, this);
    }

    public Set<String> getVariables() {
        return Collections.unmodifiableSet(scopeSupporter.getAllOwnedNames(null, this));
    }
}
