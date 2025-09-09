package redactedrice.modularparser.literal;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;

public abstract class BaseArgumentChainableLiteral extends BaseModule
        implements ChainableLiteralParser {
    protected static final String ARG_DELIMITER = ",";
    protected static final String ARG_NAME_DELIMITER = " ";
    protected static Grouper defaultGrouper;

    protected final String keyword;
    protected final String chainedArg;
    private final Grouper grouper;
    protected final String[] requiredArgs;
    protected final String[] optionalArgs;
    protected final Object[] optionalDefaults;

    protected LiteralSupporter literalSupporter;

    public static Grouper getDefaultGrouper() {
        return defaultGrouper;
    }

    public static void setDefaultGrouper(Grouper defaultGrouper) {
        BaseArgumentChainableLiteral.defaultGrouper = defaultGrouper;
    }

    protected BaseArgumentChainableLiteral(String name, String keyword, String chainedArg,
            String[] requiredArgs, String[] optionalArgs, Object[] optionalDefaults) {
        this(name, keyword, null, chainedArg, requiredArgs, optionalArgs, optionalDefaults);
    }

    protected BaseArgumentChainableLiteral(String name, String keyword, Grouper grouper,
            String chainedArg, String[] requiredArgs, String[] optionalArgs,
            Object[] optionalDefaults) {
        super(name);
        if (grouper == null) {
            if (defaultGrouper == null) {
                throw new IllegalArgumentException(
                        "Grouper cannot be null. It must be passed or the default grouper must be set");
            }
            grouper = defaultGrouper;
        }
        this.keyword = keyword.toLowerCase();
        this.chainedArg = chainedArg;
        this.grouper = grouper;
        this.requiredArgs = requiredArgs;
        this.optionalArgs = optionalArgs;
        this.optionalDefaults = optionalDefaults;
    }

    @Override
    public void setModuleRefs() {
        literalSupporter = parser.getSupporterOfType(LiteralSupporter.class);
    }

    public String getKeyword() {
        return keyword;
    }

    public Grouper getGrouper() {
        return grouper;
    }

    public String getChainedArg() {
        return chainedArg;
    }

    public String[] getRequiredArgs() {
        return requiredArgs;
    }

    public String[] getOptionalArgs() {
        return optionalArgs;
    }

    public Object[] getOptionalDefaults() {
        return optionalDefaults;
    }

    public LiteralSupporter getLiteralSupporter() {
        return literalSupporter;
    }

    protected boolean handleObjectLiteral(String literal, Map<String, Object> parsedArgs) {
        if (literal == null) {
            return false;
        }

        // Split on the first whitespace
        String[] words = literal.trim().split("(?<=\\S)\\s+", 2);
        if (words.length != 2 || !words[0].toLowerCase().equals(keyword)) {
            return false;
        }

        Response<String> argsGrouped = getGrouper().getIfCompleteGroup(words[1]);
        if (!argsGrouped.wasValueReturned()) {
            return false;
        }
        String[] args = argsGrouped.getValue().split(ARG_DELIMITER);

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
                parsedArgs.put(requiredArgs[requiredIdx++], parsed.getValue());
            } else if (optionalIdx < optionalArgs.length) {
                parsedArgs.put(optionalArgs[optionalIdx++], parsed.getValue());
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
            parsedArgs.put(entry.getKey(), parsed.getValue());
        }
        return true;
    }

    public abstract Response<Object> tryEvaluateObject(Map<String, Object> args);
}
