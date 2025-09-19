package redactedrice.modularparser.testsupport;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.BaseArgumentChainableLiteral;

public class SimpleObjectLiteralParser extends BaseArgumentChainableLiteral {
    protected static final String[] argsOrdered = new String[] {"intVal", "boolVal", "strVal",
            "so", "intArrayVal"};

    public SimpleObjectLiteralParser() {
        this(null);
    }

    public SimpleObjectLiteralParser(Grouper grouper) {
        super(SimpleObjectLiteralParser.class.getSimpleName(), "SimpleObject", grouper,
                argsOrdered[3], new String[] {argsOrdered[0]},
                new String[] {argsOrdered[1], argsOrdered[2], argsOrdered[3], argsOrdered[4]},
                new Object[] {false, "", null, null});
    }

    @Override
    public Response<Object> tryEvaluateObject(Map<String, Object> args) {
    	SimpleObject so = new SimpleObject((int) args.get(argsOrdered[0]),
                (boolean) args.get(argsOrdered[1]), (String) args.get(argsOrdered[2]),
                (SimpleObject) args.get(argsOrdered[3]));
    	if(args.get(argsOrdered[4]) != null) {
    		so.setIntArray((Integer) args.get(argsOrdered[4]));
    	}
        return Response.is(so);
    }
}
