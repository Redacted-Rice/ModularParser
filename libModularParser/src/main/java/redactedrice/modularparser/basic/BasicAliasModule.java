package redactedrice.modularparser.basic;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.AliasHandler;
import redactedrice.modularparser.base.LineStartMatchModule;

public class BasicAliasModule extends LineStartMatchModule implements AliasHandler {
    private final static Pattern aliasDef = Pattern.compile("^\\s*alias\\s+(\\w+)\\s*=\\s*(.+)$");

    private final Map<String, String> aliases = new LinkedHashMap<>();

    public BasicAliasModule() {
        super("BasicAliasHandler", "alias");
    }

    @Override
    public void handle(String line) {
        Matcher m = aliasDef.matcher(line);
        if (!m.find()) {
            return;
        }

        String key = m.group(1);
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
        System.out.println("Alias: Added alias " + key + " with value: " + val);
        aliases.put(key, val);
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
        if (matches(line))
            return line;
        String out = line;
        for (Map.Entry<String, String> e : aliases.entrySet()) {
            out = out.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b",
                    Matcher.quoteReplacement(e.getValue()));
        }
        return out;
    }

    @Override
    public boolean isAlias(String alias) {
        return aliases.containsKey(alias);
    }

    @Override
    public Set<String> getAliases() {
        return Collections.unmodifiableSet(aliases.keySet());
    }
}