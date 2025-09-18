package redactedrice.modularparser.literal;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.lineformer.Grouper;

public abstract class BaseArgumentChainableLiteral extends BaseArgumentLiteral
        implements ChainableLiteralParser {
    protected final String chainedArg;

    protected BaseArgumentChainableLiteral(String name, String keyword, String chainedArg,
            String[] requiredArgs, String[] optionalArgs, Object[] optionalDefaults) {
        this(name, keyword, null, chainedArg, requiredArgs, optionalArgs, optionalDefaults);
    }

    protected BaseArgumentChainableLiteral(String name, String keyword, Grouper grouper,
            String chainedArg, String[] requiredArgs, String[] optionalArgs,
            Object[] optionalDefaults) {
        super(name, keyword, grouper, requiredArgs, optionalArgs, optionalDefaults);
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
        int requiredIdx = 0;
        int optionalIdx = 0;
        while (requiredIdx + optionalIdx < positionalParams.size()) {
            int combined = requiredIdx + optionalIdx;
            String literal = positionalParams.get(combined);
            
            String argName;
            if (requiredIdx < requiredArgs.length) {
            	argName = requiredArgs[requiredIdx++];
            } else if (optionalIdx < optionalArgs.length) {
            	argName = optionalArgs[optionalIdx++];
            } else {
                log(LogLevel.ERROR, "Too many args were found: %s", positionalParams.toString());
                return false;
            } 
            if (argName.equals(chainedArg) && parsedArgs.containsKey(argName)) {
            	// Already added to the args
            	continue;
            }
            
            Response<Object> parsed = literalSupporter.evaluateLiteral(literal);
            if (!parsed.wasValueReturned()) {
                log(LogLevel.ERROR, "Failed to parse arg %s at index %d", literal, combined);
                return false;
            }
            parsedArgs.put(argName, parsed.getValue());
        }
        return true;
    }

    public abstract Response<Object> tryEvaluateObject(Map<String, Object> args);
}
