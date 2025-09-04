package redactedrice.modularparser.testsupport;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.BaseArgumentChainableLiteral;

public class SimpleObjectLiteralParser extends BaseArgumentChainableLiteral {
    protected static final String[] argsOrdered = new String[] {"intVal", "boolVal", "strVal",
            "so"};

    public SimpleObjectLiteralParser() {
        super(SimpleObjectLiteralParser.class.getSimpleName(), "SimpleObject", argsOrdered[3],
                new String[] {argsOrdered[0]},
                new String[] {argsOrdered[1], argsOrdered[2], argsOrdered[3]},
                new Object[] {false, "", null});
    }

    @Override
    public Response<Object> tryEvaluateObject(Map<String, Object> args) {
        try {
            return Response.is(new SimpleObject((int) args.get(argsOrdered[0]),
                    (boolean) args.get(argsOrdered[1]), (String) args.get(argsOrdered[2]),
                    (SimpleObject) args.get(argsOrdered[3])));
        } catch (ClassCastException e) {
            // This should have been handled if this was called
            return Response.error("failed to cast value: " + e.getMessage());
        }
    }
}
