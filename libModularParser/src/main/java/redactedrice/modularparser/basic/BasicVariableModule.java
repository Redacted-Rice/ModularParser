package redactedrice.modularparser.basic;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.VariableHandler;
import redactedrice.modularparser.WordReserver;

public class BasicVariableModule extends BaseModule
        implements LineHandler, VariableHandler, WordReserver {
    public enum ImplicitType {
        NONE, KEYWORD, ALL
    }

    protected final String key;
    protected final Map<String, ReservedType> reservedWords = new HashMap<>();
    protected final Map<String, Object> variables = new HashMap<>();
    protected final Pattern varDef;

    public BasicVariableModule(String moduleName, String shareableKeyword,
            String shareableQualifier, ImplicitType implicitType) {
        super(moduleName);
        key = shareableQualifier + " " + shareableKeyword;
        reservedWords.put(key, ReservedType.EXCLUSIVE);
        reservedWords.put(shareableKeyword, ReservedType.SHAREABLE);
        reservedWords.put(shareableQualifier, ReservedType.SHAREABLE);

        switch (implicitType) {
        case ALL:
            varDef = Pattern.compile("^\\s*(?:" + shareableQualifier + "\\s+)?(?:"
                    + shareableKeyword + "\\s+)?(\\w+)\\s*=\\s*(.+)$");
            break;
        case KEYWORD:
            varDef = Pattern.compile("^\\s*" + shareableQualifier + "\\s+(?:" + shareableKeyword
                    + "\\s+)?(\\w+)\\s*=\\s*(.+)$");
            break;
        default: // NONE
            varDef = Pattern.compile("^\\s*" + shareableQualifier + "\\s+" + shareableKeyword
                    + "\\s+(\\w+)\\s*=\\s*(.+)$");
            break;
        }

    }

    @Override
    public boolean matches(String logicalLine) {
        if (logicalLine == null || logicalLine.isBlank()) {
            return false;
        }
        return varDef.matcher(logicalLine).matches();
    }

    @Override
    public void handle(String line) {
        Matcher m = varDef.matcher(line);
        if (!m.find()) {
            return;  // shouldn't happen if matches() used right
        }

        // Check for previous def
        // TODO: Interface for "reserved" words - commands, alias, and vars?
        // Seems like it would make sense and make checking things easier

        String varName = m.group(1);
        String literal = m.group(2).trim();

        Object obj = parser.evaluateLiteral(literal);
        if (obj != null) {
            variables.put(varName, obj);
            System.out
                    .println(getName() + ": Added " + key + " " + varName + " with value: " + obj);
        } else {
            throw new IllegalArgumentException("VariableHandler: For variable \"" + varName
                    + "\" + cannot parse value: " + literal);
        }
    }

    @Override
    public boolean isReservedWord(String word, Optional<ReservedType> type) {
        if (type.isEmpty()) {
            return reservedWords.containsKey(word) || isVariable(word);
        } else if (type.get() == ReservedType.SHAREABLE) {
            return reservedWords.get(word) == ReservedType.SHAREABLE || isVariable(word);
        } else {
            return reservedWords.get(word) == type.get();
        }
    }

    @Override
    public Map<String, ReservedType> getAllReservedWords() {
        Map<String, ReservedType> all = new HashMap<>(reservedWords);
        getVariables().keySet().stream().forEach(alias -> all.put(alias, ReservedType.SHAREABLE));
        return Collections.unmodifiableMap(all);
    }

    @Override
    public Set<String> getReservedWords(ReservedType type) {
        Set<String> all = new HashSet<>(reservedWords.entrySet().stream()
                .filter(entry -> entry.getValue() == type).map(Map.Entry::getKey).toList());
        all.addAll(variables.keySet());
        return Collections.unmodifiableSet(all);
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        if (isVariable(literal)) {
            return Optional.ofNullable(variables.get(literal));
        }
        return Optional.empty();
    }

    @Override
    public boolean isVariable(String alias) {
        return variables.containsKey(alias);
    }

    @Override
    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(variables);
    }
}
