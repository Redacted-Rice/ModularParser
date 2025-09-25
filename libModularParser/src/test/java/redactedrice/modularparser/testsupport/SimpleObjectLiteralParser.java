package redactedrice.modularparser.testsupport;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.argumented.ArgumentsDefinition;
import redactedrice.modularparser.literal.argumented.BaseArgumentedChainableLiteral;

public class SimpleObjectLiteralParser extends BaseArgumentedChainableLiteral {
    protected static final String[] ARGS_NAMES = new String[] {"intVal", "boolVal", "strVal", "so",
            "intArrayVal"};
    protected static final ArgumentsDefinition ARGS_DEF = new ArgumentsDefinition(
            new String[] {ARGS_NAMES[0]},
            new String[] {ARGS_NAMES[1], ARGS_NAMES[2], ARGS_NAMES[3], ARGS_NAMES[4]},
            new Object[] {false, "", null, null}, null);

    public SimpleObjectLiteralParser() {
        this(null);
    }

    public SimpleObjectLiteralParser(Grouper grouper) {
        super(SimpleObjectLiteralParser.class.getSimpleName(), "SimpleObject", grouper,
                ARGS_NAMES[3], ARGS_DEF);
    }

    @Override
    public Response<Object> tryEvaluateObject(Map<String, Object> args) {
        SimpleObject so = new SimpleObject((int) args.get(ARGS_NAMES[0]),
                (boolean) args.get(ARGS_NAMES[1]), (String) args.get(ARGS_NAMES[2]),
                (SimpleObject) args.get(ARGS_NAMES[3]));
        if (args.get(ARGS_NAMES[4]) != null) {
            so.setIntArray((Integer) args.get(ARGS_NAMES[4]));
        }
        return Response.is(so);
    }
}
