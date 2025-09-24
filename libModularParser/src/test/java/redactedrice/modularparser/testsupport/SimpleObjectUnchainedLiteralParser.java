package redactedrice.modularparser.testsupport;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.argumented.BaseArgumentedLiteral;

public class SimpleObjectUnchainedLiteralParser extends BaseArgumentedLiteral {
    protected static final String[] argsOrdered = new String[] {"intVal", "boolVal", "strVal",
            "so"};

    public SimpleObjectUnchainedLiteralParser() {
        this(null);
    }

    public SimpleObjectUnchainedLiteralParser(Grouper grouper) {
        super(SimpleObjectUnchainedLiteralParser.class.getSimpleName(), "SimpleObject", grouper,
                new String[] {argsOrdered[0]},
                new String[] {argsOrdered[1], argsOrdered[2], argsOrdered[3]},
                new Object[] {false, "", null});
    }

    @Override
    public Response<Object> tryEvaluateObject(Map<String, Object> args) {
    	return Response.is(new SimpleObject((int) args.get(argsOrdered[0]),
                (boolean) args.get(argsOrdered[1]), (String) args.get(argsOrdered[2]),
                (SimpleObject) args.get(argsOrdered[3])));
    }
}
