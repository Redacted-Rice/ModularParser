package redactedrice.modularparser.scope;


import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.literal.LiteralParser;
import redactedrice.modularparser.literal.LiteralSupporter;

public class DefaultScopedVarConstParser extends BaseScopedKeywordParser implements LiteralParser {
    protected final boolean reassignmentAllowed;
    protected final Pattern matcher;

    protected LiteralSupporter literalSupporter;

    public DefaultScopedVarConstParser(String moduleName, boolean reassignmentAllowed,
            String keyword) {
        super(moduleName, keyword);
        // this.reservedWords.put(keyword, ReservedType.EXCLUSIVE);
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
            return false;
        }

        final String key = m.group(2);
        if (!isValidName(key)) {
            log(LogLevel.ERROR, "Invalid variable name: %s", key);
            return true;
        }

        // Check for collisions with reserved words outside scope supporter
        if (!ensureWordAvailableOrOwned(scope, key)) {
            return true;
        }

        // TODO: here

        if (m.group(1) == null) {
            // reassignment
            if (scope.isEmpty()) { // scope was not specified
                scope = scopeSupporter.getNarrowestScope(key);
                if (scope == null) {
                    log(LogLevel.ERROR, "Attempted to reassign undefined %s %s with %s",
                            getKeyword(), key, m.group(3));
                    return true;
                }
            }

            if (!scopeSupporter.getOwner(Optional.of(scope), key).isEmpty()) {
                if (reassignmentAllowed) {
                    addLiteral(m.group(3), scope, m.group(2), false);
                } else {
                    log(LogLevel.ERROR, "Attempted to reassign %s %s in scope %s with %s",
                            getKeyword(), m.group(2), scope, m.group(3));
                }
            } else {
                log(LogLevel.ERROR, "Attempted to reassign non-existing %s %s in scope %s with %s",
                        getKeyword(), m.group(2), scope, m.group(3));
            }
        } else {
            // Assignment
            if (scope.isEmpty()) { // scope was not specified
                scope = defaultScope;
            }

            if (scopeSupporter.getOwner(Optional.of(scope), key).isEmpty()) {
                addLiteral(m.group(3), scope, m.group(2), true);
            } else {
                log(LogLevel.ERROR, "Attempted to redefine existing %s %s in scope %s with %s",
                        getKeyword(), m.group(2), scope, m.group(3));
            }
        }
        return true;
    }

    protected void addLiteral(String literal, String scopeName, String name, boolean assignment) {
        Object obj = literalSupporter.evaluateLiteral(literal);
        if (obj != null) {
            if (scopeSupporter.setData(scopeName, name, this, obj)) {
                log(LogLevel.DEBUG, "%s %s %s in scope %s with %s",
                        (assignment ? "Added " : "Changed "), getKeyword(), name, scopeName, obj);
            }
        } else {
            log(LogLevel.ERROR, "For %s %s cannot parse value: %s", getKeyword(), name, literal);
        }
    }

    @Override
    public Optional<Object> tryParseLiteral(String literal) {
        return Optional.ofNullable(scopeSupporter.getData(Optional.empty(), literal, this));
    }

    public boolean setVariable(String scopeName, String var, Object val) {
        return scopeSupporter.setData(scopeName, var, this, val);
    }

    public boolean isVariable(String var) {
        return scopeSupporter.getData(Optional.empty(), var, this) != null;
    }

    public Set<String> getVariables() {
        return Collections.unmodifiableSet(scopeSupporter.getAllOwnedNames(Optional.empty(), this));
    }
}
