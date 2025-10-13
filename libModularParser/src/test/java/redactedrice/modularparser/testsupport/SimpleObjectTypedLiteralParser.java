package redactedrice.modularparser.testsupport;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.argumented.ArgParserAny;
import redactedrice.modularparser.literal.argumented.ArgParserTyped;
import redactedrice.modularparser.literal.argumented.ArgumentParser;
import redactedrice.modularparser.literal.argumented.ArgumentsDefinition;
import redactedrice.modularparser.literal.argumented.BaseArgumentedLiteral;

public class SimpleObjectTypedLiteralParser extends BaseArgumentedLiteral {
    protected static final String[] ARGS_NAMES = new String[] {"intVal", "boolVal", "strVal", "so",
            "intArrayVal"};
    protected static final ArgumentsDefinition argsDef = new ArgumentsDefinition(
            new String[] {ARGS_NAMES[0]},
            new String[] {ARGS_NAMES[1], ARGS_NAMES[2], ARGS_NAMES[3], ARGS_NAMES[4]},
            new Object[] {false, "", null, null},
            new ArgumentParser[] {new ArgParserTyped<>(Integer.class),
                    new ArgParserTyped<>(Boolean.class), new ArgParserAny(true),
                    new SimpleObjectArgumentParser(), new ArgParserAny(true)});

    public SimpleObjectTypedLiteralParser() {
        this(null);
    }

    public SimpleObjectTypedLiteralParser(Grouper grouper) {
        super(SimpleObjectTypedLiteralParser.class.getSimpleName(), "SimpleObject", grouper,
                argsDef);
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
