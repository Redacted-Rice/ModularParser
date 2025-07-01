package redactedrice.modularparser.basic;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.VariableHandler;

public class BasicVariableModule extends LineStartMatchModule implements VariableHandler {
    private final static Pattern varDef = Pattern.compile("^\\s*variable\\s+(\\w+)\\s*=\\s*(.+)$");

    private final Map<String, Object> variables = new LinkedHashMap<>();

    protected BasicVariableModule() {
        super("BasicVariableHandler", "variable");
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
            System.out.println("Variable: Added variable " + varName + " with value: " + obj);
        } else {
            throw new IllegalArgumentException("VariableHandler: For variable \"" + varName
                    + "\" + cannot parse value: " + literal);
        }
    }

    @Override
    public boolean isReservedWord(String word) {
        return super.isReservedWord(word) || isVariable(word);
    }

    @Override
    public Set<String> getReservedWords() {
        Set<String> all = new HashSet<>(super.getReservedWords());
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
