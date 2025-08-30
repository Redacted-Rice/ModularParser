package redactedrice.modularparser.literal;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.core.LogSupporter.LogLevel;

public abstract class BaseArgumentChainableLiteral extends BaseModule
        implements ChainableLiteralParser {
    protected final static Pattern PARAMETERS_PATTERN = Pattern.compile("(\\w+)\\s*\\(([^)]*)\\)");
    protected final static String ARG_DELIMITER = ",";
    protected final static String ARG_NAME_DELIMITER = " ";

    protected final String keyword;
    protected final String chainedArg;
    protected final String[] requiredArgs;
    protected final String[] optionalArgs;
    protected final Object[] optionalDefaults;

    protected LiteralSupporter literalSupporter;

    protected BaseArgumentChainableLiteral(String name, String keyword, String chainedArg,
            String[] requiredArgs, String[] optionalArgs, Object[] optionalDefaults) {
        super(name);
        this.keyword = keyword.toLowerCase();
        this.chainedArg = chainedArg;
        this.requiredArgs = requiredArgs;
        this.optionalArgs = optionalArgs;
        this.optionalDefaults = optionalDefaults;
    }

    @Override
    public void setModuleRefs() {
        literalSupporter = parser.getSupporterOfType(LiteralSupporter.class);
    }

    protected boolean handleObjectLiteral(String literal, Map<String, Object> parsedArgs) {
        if (literal == null) {
            return false;
        }
        String trimmed = literal.trim();

        Matcher m = PARAMETERS_PATTERN.matcher(trimmed);
        if (!m.find()) {
            return false;
        }

        String objName = m.group(1);
        String[] args = m.group(2).split(ARG_DELIMITER);

        if (!objName.toLowerCase().equals(keyword)) {
            return false;
        }

        List<String> positionalParams = new LinkedList<>();
        Map<String, String> namedParams = new HashMap<>();
        if (!parseArgs(args, positionalParams, namedParams)) {
            return false;
        }

        if (!handlePositionalArgs(positionalParams, parsedArgs)) {
            return false;
        }

        handleNamedArgs(namedParams, parsedArgs);

        for (String required : requiredArgs) {
            if (!parsedArgs.containsKey(required)) {
                log(LogLevel.ERROR, "Missing required arguement: %s", required);
                return false;
            }
        }

        for (int idx = 0; idx < optionalArgs.length; idx++) {
            if (!parsedArgs.containsKey(optionalArgs[idx])) {
                parsedArgs.put(optionalArgs[idx], optionalDefaults[idx]);
            }
        }
        return true;
    }

    @Override
    public Response<Object> tryParseLiteral(String literal) {
        Map<String, Object> parsedArgs = new HashMap<>();
        if (!handleObjectLiteral(literal, parsedArgs)) {
            return Response.notHandled();
        }
        return tryEvaluateObject(parsedArgs);
    }

    @Override
    public Response<Object> tryEvaluateChainedLiteral(Object chained, String literal) {
        Map<String, Object> parsedArgs = new HashMap<>();
        parsedArgs.put(chainedArg, chained);
        if (!handleObjectLiteral(literal, parsedArgs)) {
            return Response.notHandled();
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
            // If there is no space or its a quoted string, it doesn't have a name
            if (argSplit.length == 1 || arg.startsWith("\"")) {
                if (hasFoundNamed) {
                    log(LogLevel.ERROR, "Found positional arg after a named arg was used");
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
        int requiredIdx = 0;
        int optionalIdx = 0;
        while (requiredIdx + optionalIdx < positionalParams.size()) {
        	int combined = requiredIdx + optionalIdx;
        	String literal = positionalParams.get(combined);
            Response<Object> parsed = literalSupporter.evaluateLiteral(literal);
            if (!parsed.wasValueReturned()) {
                log(LogLevel.ERROR, "Failed to parse arg %s at index %d", literal, combined);
            	return false;
            }
            if (requiredIdx < requiredArgs.length) {
                parsedArgs.put(requiredArgs[requiredIdx++], parsed.value());
            } else if (optionalIdx < optionalArgs.length) {
                parsedArgs.put(optionalArgs[optionalIdx++], parsed.value());
            } else {
                log(LogLevel.ERROR, "Too many args were found: %s", positionalParams.toString());
                return false;
            }
        }
        return true;
    }

    protected boolean handleNamedArgs(Map<String, String> namedParams,
            Map<String, Object> parsedArgs) {
        for (Entry<String, String> entry : namedParams.entrySet()) {
            Response<Object> parsed = literalSupporter.evaluateLiteral(entry.getValue());
            if (!parsed.wasValueReturned()) {
                log(LogLevel.ERROR, "Failed to parse %s arg %s ", entry.getKey(), entry.getValue());
            	return false;
            }
            parsedArgs.put(entry.getKey(), parsed.value());
        }
        return true;
    }

    public abstract Response<Object> tryEvaluateObject(Map<String, Object> args);

}
