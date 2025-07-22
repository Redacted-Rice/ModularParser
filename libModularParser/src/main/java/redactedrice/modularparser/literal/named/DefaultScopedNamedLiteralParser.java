package redactedrice.modularparser.literal.named;


import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.literal.LiteralSupporter;
import redactedrice.modularparser.log.LogSupporter.LogLevel;
import redactedrice.modularparser.scope.BaseScopedParser;

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
    public void setModuleRefs() {
        super.setModuleRefs();
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
                    logger.log(LogLevel.ERROR, "%s: Attempted to reassign undefined %s %s with %s",
                            getName(), keyword, m.group(2), m.group(3));
                    return true;
                }
            }

            if (!isValidName(m.group(2))) {
                logger.log(LogLevel.ERROR, "%s: Invalid variable name: %s", getName(), m.group(2));
                return true;
            }

            if (!scopeSupporter.getOwner(Optional.of(scope), m.group(2)).isEmpty()) {
                if (reassignmentAllowed) {
                    addLiteral(m.group(3), scope, m.group(2), false);
                } else {
                    logger.log(LogLevel.ERROR,
                            "%s: Attempted to reassign %s %s in scope %s with %s", getName(),
                            keyword, m.group(2), scope, m.group(3));
                }
            } else {
                logger.log(LogLevel.ERROR,
                        "%s: Attempted to reassign non-existing %s %s in scope %s with %s",
                        getName(), keyword, m.group(2), scope, m.group(3));
            }
        } else {
            // Assignment
            if (scope.isEmpty()) { // scope was not specified
                scope = defaultScope;
            }

            if (!isValidName(m.group(2))) {
                logger.log(LogLevel.ERROR, "%s: Invalid variable name: %s", getName(), m.group(2));
                return true;
            }

            if (scopeSupporter.getOwner(Optional.of(scope), m.group(2)).isEmpty()) {
                addLiteral(m.group(3), scope, m.group(2), true);
            } else {
                logger.log(LogLevel.ERROR,
                        "%s: Attempted to redefine existing %s %s in scope %s with %s", getName(),
                        keyword, m.group(2), scope, m.group(3));
            }
        }
        return true;
    }

    private void addLiteral(String literal, String scopeName, String name, boolean assignment) {
        Object obj = literalHandler.evaluateLiteral(literal);
        if (obj != null) {
            if (scopeSupporter.setData(scopeName, name, this, obj)) {
                logger.log(LogLevel.DEBUG, "%s: %s %s %s in scope %s with %s", getName(),
                        (assignment ? "Added " : "Changed "), keyword, name, scopeName, obj);
            }
        } else {
            logger.log(LogLevel.ERROR, "%s: For %s %s cannot parse value: %s", getName(), keyword,
                    name, literal);
        }
    }

    @Override
    public Optional<Object> tryParseLiteral(String literal) {
        return Optional.ofNullable(scopeSupporter.getData(Optional.empty(), literal, this));
    }

    public boolean setVariable(String scopeName, String var, Object val) {
        return scopeSupporter.setData(scopeName, var, this, val);
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
