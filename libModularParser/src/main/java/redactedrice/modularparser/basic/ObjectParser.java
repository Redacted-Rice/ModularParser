package redactedrice.modularparser.basic;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.Module;
import redactedrice.modularparser.literals.LiteralParser;
import redactedrice.modularparser.literals.LiteralSupporter;

public abstract class ObjectParser extends BaseModule implements LiteralParser {
    protected final static Pattern OBJ_ARG_PATTERN = Pattern.compile("(\\w+)\\(([^)]*)\\)");
    protected final static String ARG_DELIMITER = ",";
    protected final static String ARG_NAME_DELIMITER = " ";

    protected final String keyword;
    protected final String[] requiredArgs;
    protected final String[] optionalArgs;
    protected final Object[] optionalDefaults;

    protected LiteralSupporter literalHandler;

    protected ObjectParser(String name, String keyword, String[] requiredArgs,
            String[] optionalArgs, Object[] optionalDefaults) {
        super(name);
        this.keyword = keyword.toLowerCase();
        this.requiredArgs = requiredArgs;
        this.optionalArgs = optionalArgs;
        this.optionalDefaults = optionalDefaults;
    }

    @Override
    public void configure() {
        List<Module> literalSupporters = parser.getModulesOfType(LiteralSupporter.class);
        if (literalSupporters.size() != 1) {
            throw new RuntimeException("Temp");
        }
        literalHandler = (LiteralSupporter) literalSupporters.get(0);
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        if (literal == null) {
            return Optional.empty();
        }
        String trimmed = literal.trim();

        Matcher m = OBJ_ARG_PATTERN.matcher(trimmed);
        if (!m.find()) {
            return Optional.empty();
        }

        String objName = m.group(1);
        String[] args = m.group(2).split(ARG_DELIMITER);

        if (!objName.toLowerCase().equals(keyword)) {
            return Optional.empty();
        }

        List<String> positionalParams = new LinkedList<>();
        Map<String, String> namedParams = new HashMap<>();
        if (!parseArgs(args, positionalParams, namedParams)) {
            return Optional.empty();
        }

        Map<String, Object> parsedArgs = new HashMap<>();
        if (!handlePositionalArgs(positionalParams, parsedArgs)) {
            return Optional.empty();
        }

        if (!handleNamedArgs(namedParams, parsedArgs)) {
            return Optional.empty();
        }

        for (String required : requiredArgs) {
            if (!parsedArgs.containsKey(required)) {
                System.err.println("Missing required arguement: " + required);
                return Optional.empty();
            }
        }

        for (int idx = 0; idx < optionalArgs.length; idx++) {
            if (!parsedArgs.containsKey(optionalArgs[idx])) {
                parsedArgs.put(optionalArgs[idx], optionalDefaults[idx]);
            }
        }

        return tryEvaluateObject(parsedArgs);
    }

    protected boolean parseArgs(String[] args, List<String> positionalParams,
            Map<String, String> namedParams) {
        boolean hasFoundNamed = false;
        for (String arg : args) {
            arg = arg.trim();
            if (arg.isEmpty()) {
                continue;
            }
            String[] argSplit = arg.split(ARG_NAME_DELIMITER, 2);
            if (argSplit.length == 1 || arg.startsWith("\"") && arg.endsWith("\"")) {
                if (hasFoundNamed) {
                    System.err.println("Found positional arg after a named arg was used");
                    return false;
                }
                positionalParams.add(arg);
            } else {
                namedParams.put(argSplit[0], argSplit[1]);
                hasFoundNamed = true;
            }
        }
        return true;
    }

    protected boolean handlePositionalArgs(List<String> positionalParams,
            Map<String, Object> parsedArgs) {
        int positionalIdx = 0;
        int optionalIdx = 0;
        while (positionalIdx + optionalIdx < positionalParams.size()) {
            Object parsed = literalHandler
                    .evaluateLiteral(positionalParams.get(positionalIdx + optionalIdx));
            if (positionalIdx < requiredArgs.length) {
                parsedArgs.put(requiredArgs[positionalIdx++], parsed);
            } else if (optionalIdx < optionalArgs.length) {
                parsedArgs.put(optionalArgs[optionalIdx++], parsed);
            } else {
                System.err.println("Too many args were found");
                return false;
            }
        }
        return true;
    }

    protected boolean handleNamedArgs(Map<String, String> namedParams,
            Map<String, Object> parsedArgs) {
        for (Entry<String, String> entry : namedParams.entrySet()) {
            parsedArgs.put(entry.getKey(), literalHandler.evaluateLiteral(entry.getValue()));
        }
        return true;
    }

    public abstract Optional<Object> tryEvaluateObject(Map<String, Object> args);

}
