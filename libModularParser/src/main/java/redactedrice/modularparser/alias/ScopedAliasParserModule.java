package redactedrice.modularparser.alias;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.lineformer.LineModifier;
import redactedrice.modularparser.reserved.WordReserver;
import redactedrice.modularparser.scope.BaseScopedModule;

public class ScopedAliasParserModule extends BaseScopedModule implements LineModifier, WordReserver, AliasParser {
    private final Pattern aliasDef;

    protected final String keyword;
    

    public ScopedAliasParserModule() {
        super("BasicAliasHandler");
        keyword = "alias";
        aliasDef = Pattern.compile("^\\s*" + keyword + "\\s+(\\w+)\\s*=\\s*(.+)$");
    }
    
    @Override
    public boolean hasOpenModifier(String line) {
        return false;
    }

    @Override
    public String modifyLine(String line) {
        String out = line;
        for (Map.Entry<String, Object> e : scopeSupporter.getAllOwnedData(Optional.empty(), getName())
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
            System.err.println("Invalid alias name: " + key);
            return true;
        }

        String val = m.group(2).trim();
        // strip quotes
        if ((val.startsWith("\"") && val.endsWith("\""))
                || (val.startsWith("'") && val.endsWith("'"))) {
            val = val.substring(1, val.length() - 1);
        }

        // Check for collisions with reserved words
//        if (parser.getAllReservedWords().containsKey(key)) {
//            System.err.println(
//                    "Warning: alias '" + key + "' conflicts reserved word and will be ignored!");
//            return true;
//        }

        if (scopeSupporter.setData(scope, key, getName(), val)) {
            System.out.println("Alias: Added alias " + key + " with value: " + val);
        }
        return true;
    }

    @Override
    public boolean isReservedWord(String word, Optional<ReservedType> type) {
        if (type.isEmpty() || type.get() == ReservedType.EXCLUSIVE) {
            return word.equals(type) || isAlias(word);
        }
        return false;
    }

    @Override
    public Map<String, ReservedType> getAllReservedWords() {
        HashMap<String, ReservedType> all = new HashMap<>();
        all.put(keyword, ReservedType.EXCLUSIVE);
        getAliases().stream().forEach(alias -> all.put(alias, ReservedType.EXCLUSIVE));
        return Collections.unmodifiableMap(all);
    }

    @Override
    public Set<String> getReservedWords(ReservedType type) {
        if (type == ReservedType.EXCLUSIVE) {
            Set<String> all = new HashSet<>();
            all.add(keyword);
            all.addAll(getAliases());
            return Collections.unmodifiableSet(all);
        }
        return Collections.emptySet();
    }

    @Override
    public String replaceAliases(String line) {
        return modifyLine(line);
    }

    @Override
    public boolean isAlias(String alias) {
        return scopeSupporter.getData(Optional.empty(), alias, getName()) != null;
    }

    @Override
    public Set<String> getAliases() {
        return Collections
                .unmodifiableSet(scopeSupporter.getAllOwnedNames(Optional.empty(), getName()));
    }
}