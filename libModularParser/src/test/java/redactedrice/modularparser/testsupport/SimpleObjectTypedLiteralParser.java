package redactedrice.modularparser.testsupport;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.argumented.ArgumentParser;
import redactedrice.modularparser.literal.argumented.BaseArgumentedLiteral;
import redactedrice.modularparser.literal.argumented.TypeEnforcer;
import redactedrice.modularparser.literal.argumented.TypeEnforcerNonNullable;
import redactedrice.modularparser.literal.argumented.TypeUnenforced;

public class SimpleObjectTypedLiteralParser extends BaseArgumentedLiteral {
    protected static final String[] argsOrdered = new String[] {"intVal", "boolVal", "strVal", "so",
            "intArrayVal"};

    public SimpleObjectTypedLiteralParser() {
        this(null);
    }

    public SimpleObjectTypedLiteralParser(Grouper grouper) {
        super(SimpleObjectTypedLiteralParser.class.getSimpleName(), "SimpleObject", grouper,
                new String[] {argsOrdered[0]},
                new String[] {argsOrdered[1], argsOrdered[2], argsOrdered[3], argsOrdered[4]},
                new Object[] {false, "", null, null},
                new ArgumentParser[] {new TypeEnforcer<>(Integer.class),
                        new TypeEnforcerNonNullable<>(Boolean.class), new TypeUnenforced(),
                        new SimpleObjectArgumentParser(), new TypeUnenforced()});
    }

    @Override
    public Response<Object> tryEvaluateObject(Map<String, Object> args) {
        SimpleObject so = new SimpleObject((int) args.get(argsOrdered[0]),
                (boolean) args.get(argsOrdered[1]), (String) args.get(argsOrdered[2]),
                (SimpleObject) args.get(argsOrdered[3]));
        if (args.get(argsOrdered[4]) != null) {
            so.setIntArray((Integer) args.get(argsOrdered[4]));
        }
        return Response.is(so);
    }
}
