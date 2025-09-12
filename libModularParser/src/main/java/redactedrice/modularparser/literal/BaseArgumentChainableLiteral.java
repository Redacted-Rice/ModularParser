package redactedrice.modularparser.literal;


import java.util.HashMap;
import java.util.Map;

import redactedrice.modularparser.core.Response;
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

    public abstract Response<Object> tryEvaluateObject(Map<String, Object> args);
}
