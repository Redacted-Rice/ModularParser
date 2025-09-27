package redactedrice.modularparser.literal.argumented;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.LiteralParser;
import redactedrice.modularparser.literal.LiteralSupporter;

public abstract class BaseArgumentedLiteral extends BaseModule implements LiteralParser {
    protected static final String ARG_DELIMITER = ",";
    protected static final String ARG_NAME_DELIMITER = " ";
    protected static Grouper defaultGrouper;

    protected final String keyword;
    protected final Grouper grouper;
    protected final ArgumentsDefinition argsDef;

    protected LiteralSupporter literalSupporter;

    public static Grouper getDefaultGrouper() {
        return defaultGrouper;
    }

    public static void setDefaultGrouper(Grouper defaultGrouper) {
        BaseArgumentedLiteral.defaultGrouper = defaultGrouper;
    }

    protected BaseArgumentedLiteral(String name, String keyword, ArgumentsDefinition arguments) {
        this(name, keyword, null, arguments);
    }

    protected BaseArgumentedLiteral(String name, String keyword, Grouper grouper,
            ArgumentsDefinition argsDef) {
        super(name);
        if (grouper == null) {
            if (defaultGrouper == null) {
                throw new IllegalArgumentException(
                        "Grouper cannot be null. It must be passed or the default grouper must be set");
            }
            grouper = defaultGrouper;
        }
        this.argsDef = argsDef;
        this.keyword = keyword.toLowerCase();
        this.grouper = grouper;
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

    public ArgumentsDefinition getArgsDef() {
        return argsDef;
    }

    public LiteralSupporter getLiteralSupporter() {
        return literalSupporter;
    }

    protected boolean handleObjectLiteral(String literal, Map<String, Object> parsedArgs) {
        if (literal == null || literal.isEmpty()) {
            return false;
        }

        // Split on the first whitespace
        String[] words = literal.trim().split("\\s+", 2);
        if (words.length != 2 || !words[0].toLowerCase().equals(keyword)) {
            return false;
        }

        Response<String[]> args = grouper.tryGetNextGroup(words[1], true);
        // Ensure we got a return for a complete group with nothing before or after
        if (!args.wasValueReturned() || !args.getValue()[0].isBlank() ||
                !args.getValue()[2].isBlank()) {
            return false;
        }

        List<String> positionalParams = new LinkedList<>();
        Map<String, String> namedParams = new HashMap<>();
        if (!parseArgs(args.getValue()[1], positionalParams, namedParams)) {
            return false;
        }

        if (!handlePositionalArgs(positionalParams, parsedArgs)) {
            return false;
        }

        if (!handleNamedArgs(namedParams, parsedArgs)) {
            return false;
        }

        for (int idx = 0; idx < argsDef.getNumRequiredArgs(); idx++) {
            String required = argsDef.getRequiredArg(idx);
            if (!parsedArgs.containsKey(required)) {
                log(LogLevel.ERROR, "Missing required arguement: %s", required);
                return false;
            }
        }

        for (int idx = 0; idx < argsDef.getNumOptionalArgs(); idx++) {
            parsedArgs.putIfAbsent(argsDef.getOptionalArg(idx), argsDef.getOptionalDefault(idx));
        }
        return true;
    }

    @Override
    public Response<Object> tryParseLiteral(String literal) {
        Map<String, Object> parsedArgs = new HashMap<>();
        if (!handleObjectLiteral(literal, parsedArgs)) {
            return Response.notHandled();
        }
        try {
            return tryEvaluateObject(parsedArgs);
        } catch (ClassCastException e) {
            log(LogLevel.ERROR, e,
                    "Incompatible types were passed causing class cast exception for literal "
                            + literal);
            return Response.error("ClassCastException while parsing " + literal);
        }
    }

    protected boolean parseArgs(String args, List<String> positionalParams,
            Map<String, String> namedParams) {
        boolean hasFoundNamed = false;
        while (!args.isBlank()) {
            // Split on arg separators
            String[] argPopped = args.split(ARG_DELIMITER, 2);
            // If it has an open group, then we need to parse it differently instead
            String arg = argPopped[0];
            String restOfParams = "";
            if (grouper.hasOpenGroup(arg)) {
                // Get the next group and break it by that instead
                Response<String[]> group = grouper.tryGetNextGroup(args, false);
                if (!group.wasValueReturned()) {
                    log(LogLevel.ERROR,
                            "Ill formed arg. Found grouper but was not closed: " + args);
                    return false;
                }
                String[] foundGroup = group.getValue();
                arg = foundGroup[0];
                restOfParams = " " + foundGroup[1].trim();
                args = group.getValue()[2].trim();
            } else {
                // Its not an open group meaning its a standard arg or a object/function with
                // no params. We can use the existing break and just need to update the args
                args = argPopped.length > 1 ? argPopped[1].trim() : "";
            }
            Response<Boolean> wasNamed = tryAddParam(arg.trim(), restOfParams, positionalParams,
                    namedParams, hasFoundNamed);
            if (wasNamed.wasError()) {
                log(LogLevel.ERROR, wasNamed.getError());
                return false;
            }
            hasFoundNamed = hasFoundNamed || wasNamed.getValue();
        }
        return true;
    }

    protected Response<Boolean> tryAddParam(String arg, String restOfParams,
            List<String> positionalParams, Map<String, String> namedParams, boolean hasFoundNamed) {
        // Split on the arg <-> name splitter
        String[] tryNameSplit = arg.split(ARG_NAME_DELIMITER, 2);
        if (tryNameSplit.length > 1 && grouper.isEmptyGroup(tryNameSplit[1])) {
            // No name, empty fn
            if (hasFoundNamed) {
                return Response.error(
                        "Found positional void object/function arg after a named arg was used");
            }
            positionalParams.add(arg);
            return Response.is(false);
        } else if (tryNameSplit.length == 1 || arg.startsWith("\"") || arg.startsWith("'")) {
            // If there is no space or its a quoted string, it doesn't have a name
            if (hasFoundNamed) {
                return Response.error("Found positional literal arg after a named arg was used");
            }
            positionalParams.add(arg + restOfParams);
            return Response.is(false);
        } else {
            // Otherwise the group is a later arg and this is named
            namedParams.put(tryNameSplit[0], tryNameSplit[1] + restOfParams);
            return Response.is(true);
        }
    }

    protected boolean handlePositionalArgs(List<String> positionalParams,
            Map<String, Object> parsedArgs) {
        for (int argIdx = 0; argIdx < positionalParams.size(); argIdx++) {
            String literal = positionalParams.get(argIdx);
            String argName = argsDef.getArg(argIdx);
            if (argName == null) {
                log(LogLevel.ERROR, "Too many args were found: %s", positionalParams.toString());
                return false;
            }

            if (!tryParseArgument(argName, literal, parsedArgs)) {
                return false;
            }
        }
        return true;
    }

    protected boolean handleNamedArgs(Map<String, String> namedParams,
            Map<String, Object> parsedArgs) {
        for (Entry<String, String> entry : namedParams.entrySet()) {
            if (!tryParseArgument(entry.getKey(), entry.getValue(), parsedArgs)) {
                return false;
            }
        }
        return true;
    }

    protected boolean tryParseArgument(String argName, String argument,
            Map<String, Object> parsedArgs) {
        ArgumentParser argParser = argsDef.getArgParser(argName);
        if (argParser == null) {
            log(LogLevel.ERROR, "Internal error: Failed to find parser for arg %s", argName);
            return false;
        }

        Response<Object> parsed = literalSupporter.evaluateLiteral(argument);
        parsed = argParser.tryParseArgument(parsed, argument);

        if (parsed.wasError()) {
            log(LogLevel.ERROR, "Failed to parse arg %s with value %s: %s", argName, argument, parsed.getError());
            return false;
        }
        parsedArgs.put(argName, parsed.getValue());
        return true;
    }

    public abstract Response<Object> tryEvaluateObject(Map<String, Object> args)
            throws ClassCastException;
}
