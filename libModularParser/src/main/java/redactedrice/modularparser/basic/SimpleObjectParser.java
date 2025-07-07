package redactedrice.modularparser.basic;


import java.util.Map;
import java.util.Optional;

public class SimpleObjectParser extends ObjectParser {
    private final static String[] argsOrdered = new String[] { "intVal", "boolVal", "strVal"
    };

    public SimpleObjectParser() {
        super("SimpleObjectParser", "SimpleObject", new String[] {}, argsOrdered,
                new Object[] { 5, false, ""
                });
    }

    @Override
    public Optional<Object> tryEvaluateObject(Map<String, Object> args) {
        return Optional.of(new SimpleObject((int) args.get(argsOrdered[0]),
                (boolean) args.get(argsOrdered[1]), (String) args.get(argsOrdered[2])));
    }
}
