package redactedrice.modularparser.literal;


import java.util.Map;
import java.util.Optional;

import redactedrice.modularparser.basic.SimpleObject;

public class SimpleObjectLiteralParserModule extends ChainableParametersModule {
    private final static String[] argsOrdered = new String[] {"intVal", "boolVal", "strVal", "so"
    };

    public SimpleObjectLiteralParserModule() {
        super("SimpleObjectParser", "SimpleObject", argsOrdered[3], new String[] { argsOrdered[0]
        }, new String[] { argsOrdered[1], argsOrdered[2], argsOrdered[3]
        }, new Object[] { false, "", null
        });
    }

    @Override
    public Optional<Object> tryEvaluateObject(Map<String, Object> args) {
        return Optional.of(new SimpleObject((int) args.get(argsOrdered[0]),
                (boolean) args.get(argsOrdered[1]), (String) args.get(argsOrdered[2]),
                (SimpleObject) args.get(argsOrdered[3])));
    }
}
