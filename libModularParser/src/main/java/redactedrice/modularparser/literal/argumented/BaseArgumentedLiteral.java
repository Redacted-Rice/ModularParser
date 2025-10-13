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
        String[] argSplit;
        while (!args.isBlank()) {
            argSplit = popNextArg(args);
            if (argSplit.length < 2) {
                log(LogLevel.ERROR, "TODO Error");
                return false;
            }
            args = argSplit[1];

            Response<Boolean> wasNamed = tryAddParam(argSplit[0], positionalParams, namedParams,
                    hasFoundNamed);
            if (wasNamed.wasError()) {
                log(LogLevel.ERROR, wasNamed.getError());
                return false;
            }
            hasFoundNamed = hasFoundNamed || wasNamed.getValue();
        }
        return true;
    }

    protected String[] popNextArg(String args) {
        // Split on arg separators
        String[] argPopped = args.split(ARG_DELIMITER, 2);
        // If it has an open group, then we need to parse it differently instead
        String arg = argPopped[0].trim();
        if (grouper.hasOpenGroup(arg)) {
            do {
                if (argPopped[1].isBlank()) {
                    log(LogLevel.ERROR,
                            "Ill formed arg. Found arg delimiter in grouper but could not find end of arg: "
                                    + args);
                    return new String[0];
                }
                argPopped = argPopped[1].split(ARG_DELIMITER, 2);
                arg += ARG_DELIMITER + " " + argPopped[0].trim();
            } while (grouper.hasOpenGroup(arg));
        }
        String remainder = argPopped.length > 1 ? argPopped[1].trim() : "";
        return new String[] {arg, remainder};
    }

    protected Response<Boolean> tryAddParam(String arg, List<String> positionalParams,
            Map<String, String> namedParams, boolean hasFoundNamed) {
        if (arg.startsWith("\"")) {
            if (!arg.endsWith("\"")) {
                return Response.error("Bad String");
            }
            return addPositionalParam(arg, positionalParams, hasFoundNamed);
        } else if (arg.startsWith("'")) {
            if (!arg.endsWith("'")) {
                return Response.error("Bad char");
            }
            return addPositionalParam(arg, positionalParams, hasFoundNamed);
        }

        String[] tryNameSplit = arg.split(ARG_NAME_DELIMITER, 2);
        if (tryNameSplit.length == 1 ||
                (tryNameSplit.length > 1 && grouper.startsWithAGroup(tryNameSplit[1]))) {
            return addPositionalParam(arg, positionalParams, hasFoundNamed);
        } else {
            // Otherwise the group is a later arg and this is named
            namedParams.put(tryNameSplit[0], tryNameSplit[1]);
            return Response.is(true);
        }
    }

    protected Response<Boolean> addPositionalParam(String arg, List<String> positionalParams,
            boolean hasFoundNamed) {
        if (hasFoundNamed) {
            return Response
                    .error("Found positional literal arg '" + arg + "' after a named arg was used");
        }
        positionalParams.add(arg);
        return Response.is(false);
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
        ArgumentParser argParser = getArgParser(argName);
        if (argParser == null) {
            log(LogLevel.ERROR, "Internal error: Failed to find parser for arg %s", argName);
            return false;
        }
        if (argParser instanceof ArgUnparsed) {
            parsedArgs.put(argName, argument);
        } else {
            Response<Object> parsed = getLiteralSupporter().evaluateLiteral(argument);
            parsed = argParser.tryParseArgument(parsed, argument);

            if (parsed.wasError()) {
                log(LogLevel.ERROR, "Failed to parse arg '%s' with value '%s': %s", argName,
                        argument, parsed.getError());
                return false;
            } else if (parsed.wasNotHandled()) {
                log(LogLevel.ERROR, "Failed to parse arg '%s' with value '%s'. Unspecified error",
                        argName, argument);
                return false;
            }
            parsedArgs.put(argName, parsed.getValue());
        }
        return true;
    }

    protected ArgumentParser getArgParser(String name) {
        return argsDef.getArgParser(name);
    }

    public abstract Response<Object> tryEvaluateObject(Map<String, Object> args)
            throws ClassCastException;
}
