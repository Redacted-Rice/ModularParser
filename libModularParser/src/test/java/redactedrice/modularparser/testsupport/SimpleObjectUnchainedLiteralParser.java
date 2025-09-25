package redactedrice.modularparser.testsupport;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.argumented.ArgumentsDefinition;
import redactedrice.modularparser.literal.argumented.BaseArgumentedLiteral;

public class SimpleObjectUnchainedLiteralParser extends BaseArgumentedLiteral {
    protected static final String[] ARGS_NAMES = new String[] {"intVal", "boolVal", "strVal", "so"};
    protected static final ArgumentsDefinition argsDef = new ArgumentsDefinition(
            new String[] {ARGS_NAMES[0]},
            new String[] {ARGS_NAMES[1], ARGS_NAMES[2], ARGS_NAMES[3]},
            new Object[] {false, "", null}, null);

    public SimpleObjectUnchainedLiteralParser() {
        this(null);
    }

    public SimpleObjectUnchainedLiteralParser(Grouper grouper) {
        super(SimpleObjectUnchainedLiteralParser.class.getSimpleName(), "SimpleObject", grouper,
                argsDef);
    }

    @Override
    public Response<Object> tryEvaluateObject(Map<String, Object> args) {
        return Response.is(
                new SimpleObject((int) args.get(ARGS_NAMES[0]), (boolean) args.get(ARGS_NAMES[1]),
                        (String) args.get(ARGS_NAMES[2]), (SimpleObject) args.get(ARGS_NAMES[3])));
    }
}
