package redactedrice.modularparser.basic;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.AliasHandler;
import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.ScopeHandler;

public class BasicScopedAliasModule extends ScopedModule
implements LineHandler, AliasHandler {
    private final Pattern aliasDef;
    
    protected final String keyword;

    public BasicScopedAliasModule(ScopeHandler scopeHandler) {
        super("BasicAliasHandler", scopeHandler);
        keyword = "alias";
        aliasDef = Pattern.compile("^\\s*" + keyword + "\\s+(\\w+)\\s*=\\s*(.+)$");
    }
    
    @Override
    public boolean scopedMatches(String scope, String logicalLine) {
        Matcher m = aliasDef.matcher(logicalLine);
        return m.find();
    }
    
    @Override
    public void scopedHandle(String scope, String logicalLine, String defaultScope) {
        if (scope.isEmpty()) {
        	scope = defaultScope;
        }
        
        Matcher m = aliasDef.matcher(logicalLine);
        if (!m.find()) {
            return;
        }

        String key = m.group(1);
        if (!isValidName(key)) {
            System.err.println("Invalid alias name: " + key);
            return;
        }
        
        String val = m.group(2).trim();
        // strip quotes
        if ((val.startsWith("\"") && val.endsWith("\""))
                || (val.startsWith("'") && val.endsWith("'"))) {
            val = val.substring(1, val.length() - 1);
        }

        // Check for collisions with reserved words
        if (parser.getAllReservedWords().containsKey(key)) {
            System.err.println(
                    "Warning: alias '" + key + "' conflicts reserved word and will be ignored!");
            return;
        }

        // Check for collisions with already defined alias
        if (parser.getAllAliases().contains(key)) {
            System.err.println("Warning: alias '" + key
                    + "' conflicts already defined alias and will be ignored!");
            return;
        }
        
        if (scopeHandler.setData(scope, key, getName(), val)) {
            System.out.println("Alias: Added alias " + key + " with value: " + val);
        }
    }

    @Override
    public boolean isReservedWord(String word, Optional<ReservedType> type) {
        if (type.isEmpty() || type.get() == ReservedType.EXCLUSIVE) {
            return super.isReservedWord(word) || isAlias(word);
        }
        return false;
    }

    @Override
    public Map<String, ReservedType> getAllReservedWords() {
        HashMap<String, ReservedType> all = new HashMap<>(super.getAllReservedWords());
        getAliases().stream().forEach(alias -> all.put(alias, ReservedType.EXCLUSIVE));
        return Collections.unmodifiableMap(all);
    }

    @Override
    public Set<String> getReservedWords(ReservedType type) {
        if (type == ReservedType.EXCLUSIVE) {
            Set<String> all = new HashSet<>(super.getReservedWords(type));
            all.addAll(getAliases());
            return Collections.unmodifiableSet(all);
        }
        return Collections.emptySet();
    }

    @Override
    public String replaceAliases(String line) {
        String out = line;
        for (Map.Entry<String, Object> e : scopeHandler.getAllOwnedData(Optional.empty(), getName()).entrySet()) {
            out = out.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b",
                    Matcher.quoteReplacement((String)e.getValue()));
        }
        return out;
    }

    @Override
    public boolean isAlias(String alias) {
        return scopeHandler.getData(Optional.empty(), alias, getName()) != null;
    }

    @Override
    public Set<String> getAliases() {
        return Collections.unmodifiableSet(scopeHandler.getAllOwnedNames(Optional.empty(), getName()));
    }
}