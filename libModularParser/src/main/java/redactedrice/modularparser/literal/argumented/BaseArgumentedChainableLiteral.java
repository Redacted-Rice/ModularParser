package redactedrice.modularparser.literal.argumented;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.ChainableLiteralParser;

public abstract class BaseArgumentedChainableLiteral extends BaseArgumentedLiteral
        implements ChainableLiteralParser {
    protected final String chainedArg;

    protected BaseArgumentedChainableLiteral(String name, String keyword, String chainedArg,
            ArgumentsDefinition arguments) {
        this(name, keyword, null, chainedArg, arguments);
    }

    protected BaseArgumentedChainableLiteral(String name, String keyword, Grouper grouper,
            String chainedArg, ArgumentsDefinition arguments) {
        super(name, keyword, grouper, arguments);
        this.chainedArg = chainedArg;
    }

    public String getChainedArg() {
        return chainedArg;
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

    @Override
    protected boolean handlePositionalArgs(List<String> positionalParams,
            Map<String, Object> parsedArgs) {
        int argIdx = 0;
        for (int positionalIdx = 0; positionalIdx < positionalParams.size(); positionalIdx++) {
            String literal = positionalParams.get(argIdx);
            String argName = argsDef.getArg(argIdx);
            if (chainedArg.equals(argName) && parsedArgs.containsKey(argName)) {
                argIdx++;
                argName = argsDef.getArg(argIdx);
            }
            if (argName == null) {
                log(LogLevel.ERROR, "Too many args were found: %s", positionalParams.toString());
                return false;
            }

            if (!tryParseArgument(argName, literal, parsedArgs)) {
                return false;
            }
            argIdx++;
        }
        return true;
    }

    public abstract Response<Object> tryEvaluateObject(Map<String, Object> args);
}
