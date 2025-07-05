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

public class BasicScopedAliasModule extends ReservedWordModule
implements LineHandler, AliasHandler {
    private final Pattern aliasDef;
    
    protected final String keyword;

    public BasicScopedAliasModule() {
        super("BasicAliasHandler");
        keyword = "alias";
        aliasDef = Pattern.compile("^\\s*" + keyword + "\\s+(\\w+)\\s*=\\s*(.+)$");
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
        // todo use matcher - replace line start with a custom matcher
        Matcher m = aliasDef.matcher(split[1]);
        return m.find();
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
        if (scopeLine[0].isEmpty()) {
        	scopeLine[0] = scope.currentScope();
        }
        
        Matcher m = aliasDef.matcher(scopeLine[1]);
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
        
        if (scope.setData(scopeLine[0], key, getName(), val)) {
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
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return line;
        }
        
        String out = line;
        for (Map.Entry<String, Object> e : scope.getAllOwnedData(Optional.empty(), getName()).entrySet()) {
            out = out.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b",
                    Matcher.quoteReplacement((String)e.getValue()));
        }
        return out;
    }

    @Override
    public boolean isAlias(String alias) {
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return false;
        }
        return scope.getData(Optional.empty(), alias, getName()) != null;
    }

    @Override
    public Set<String> getAliases() {
        ScopeHandler scope = parser.getScoperFor(getName());
        if (scope == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(scope.getAllOwnedNames(Optional.empty(), getName()));
    }
}