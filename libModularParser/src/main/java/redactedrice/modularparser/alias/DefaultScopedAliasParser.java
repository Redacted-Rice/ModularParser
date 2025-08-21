package redactedrice.modularparser.alias;


import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.lineformer.LineModifier;
import redactedrice.modularparser.literal.named.DefaultScopedNamedLiteralParser;
import redactedrice.modularparser.scope.BaseScopedKeywordParser;

public class DefaultScopedAliasParser extends BaseScopedKeywordParser
        implements LineModifier, AliasParser {
    private final Pattern aliasDef;

    public DefaultScopedAliasParser() {
        super("BasicAliasHandler", "alias");
        aliasDef = Pattern.compile("^\\s*" + getKeyword() + "\\s+(\\w+)\\s*=\\s*(.+)$");
    }

    public static boolean isValidName(String name) {
        return DefaultScopedNamedLiteralParser.isValidName(name);
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
        for (Map.Entry<String, Object> e : scopeSupporter.getAllOwnedData(Optional.empty(), this)
                .entrySet()) {
            out = out.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b",
                    Matcher.quoteReplacement((String) e.getValue()));
        }
        return out;
    }

    @Override
    public boolean tryParseScoped(String scope, String logicalLine, String defaultScope) {
        if (scope.isEmpty()) {
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

        String val = m.group(2).trim();
        // strip quotes
        if ((val.startsWith("\"") && val.endsWith("\"")) ||
                (val.startsWith("'") && val.endsWith("'"))) {
            val = val.substring(1, val.length() - 1);
        } else if (val.startsWith("\"") || val.endsWith("\"") || val.startsWith("'") ||
                val.endsWith("'")) {
            log(LogLevel.ERROR, "Invalid alias definition - mismatched or only one quote: %s", key);
            return true;
        }

        // Check for collisions with reserved words
        // if (parser.getAllReservedWords().containsKey(key)) {
        // System.err.println(
        // "Warning: alias '" + key + "' conflicts reserved word and will be ignored!");
        // return true;
        // }

        if (scopeSupporter.setData(scope, key, this, val)) {
            log(LogLevel.DEBUG, "Added alias %s with value: %s", key, val);
        }
        return true;
    }

    @Override
    public String replaceAliases(String line) {
        return modifyLine(line);
    }

    public boolean setAlias(String scopeName, String alias, String val) {
        return scopeSupporter.setData(scopeName, alias, this, val);
    }

    @Override
    public boolean isAlias(String alias) {
        return scopeSupporter.getData(Optional.empty(), alias, this) != null;
    }

    @Override
    public Set<String> getAliases() {
        return Collections.unmodifiableSet(scopeSupporter.getAllOwnedNames(Optional.empty(), this));
    }
}