package redactedrice.modularparser.scope;


import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.LineModifier;

public class DefaultScopedAliasParser extends BaseScopedKeywordParser implements LineModifier {
    protected final Pattern aliasDef;

    public DefaultScopedAliasParser() {
        super(DefaultScopedAliasParser.class.getSimpleName(), "alias");
        aliasDef = Pattern.compile("^\\s*" + getKeyword() + "\\s+(\\w+)\\s*=\\s*(.+)$");
    }

    @Override
    public boolean lineContinuersValid(String line, boolean isComplete) {
        return true;
    }

    @Override
    public boolean lineHasOpenModifier(String line) {
        return false;
    }

    @Override
    public String modifyLine(String line) {
        String out = line;
        for (Map.Entry<String, Object> e : scopeSupporter.getAllOwnedData(null, this).entrySet()) {
            out = out.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b",
                    Matcher.quoteReplacement((String) e.getValue()));
        }
        return out;
    }

    @Override
    public boolean tryParseScoped(String scope, String logicalLine, String defaultScope) {
        if (scope == null || scope.isBlank()) {
            scope = defaultScope;
        }

        Matcher m = aliasDef.matcher(logicalLine);
        if (!m.find()) {
            return false;
        }

        String key = m.group(1);
        if (!isValidName(key)) {
            log(LogLevel.ERROR, "Invalid alias name: %s", key);
            return true;
        }

        // Check for collisions with reserved words outside scope supporter
        if (!ensureWordAvailableOrOwned(scope, key)) {
            return true;
        }

        Response<String> wordOwner = scopeSupporter.getOwner(scope, name);
        if (wordOwner.wasValueReturned()) {
            log(LogLevel.ERROR, "Alias '%s' already defined in scope '%s' by '%s'!", name, scope,
                    wordOwner.value());
            return true;
        } else if (wordOwner.wasError()) {
            log(LogLevel.ERROR, "Alias '%s' error retrieving owner in scope '%s': %s", name, scope,
                    wordOwner.getError());
            return true;
        }

        String val = m.group(2).trim();
        // strip quotes
        if ((val.startsWith("\"") && val.endsWith("\"")) ||
                (val.startsWith("'") && val.endsWith("'"))) {
            val = val.substring(1, val.length() - 1);
        } else if (val.startsWith("\"") || val.endsWith("\"") || val.startsWith("'") ||
                val.endsWith("'")) {
            log(LogLevel.ERROR,
                    "Invalid alias definition - name %s mismatched or only one quote: %s", key,
                    val);
            return true;
        }

        if (scopeSupporter.setData(scope, key, this, val)) {
            log(LogLevel.DEBUG, "Added alias %s with value: %s", key, val);
        }
        return true;
    }

    public boolean setAlias(String scopeName, String alias, String val) {
        return scopeSupporter.setData(scopeName, alias, this, val);
    }

    public boolean isAlias(String alias) {
        return scopeSupporter.getData(null, alias, this).wasHandled();
    }

    public Response<Object> getAliasValue(String alias) {
        // TODO: Aliases are only strings
        return scopeSupporter.getData(null, alias, this);
    }

    public Set<String> getAliases() {
        return Collections.unmodifiableSet(scopeSupporter.getAllOwnedNames(null, this));
    }
}