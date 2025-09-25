package redactedrice.modularparser.reflectionhelpersparsers;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.argumented.ArgumentsDefinition;
import redactedrice.modularparser.literal.argumented.BaseArgumentedChainableLiteral;
import redactedrice.reflectionhelpers.objects.ExtendableObject;

public class ExtendableObjectParser extends BaseArgumentedChainableLiteral {
    private static final String[] ARG_NAMES = new String[] {"object"};
    protected static final ArgumentsDefinition ARGS_DEF = new ArgumentsDefinition(
            new String[] {ARG_NAMES[0]}, null, null, null);

    public ExtendableObjectParser() {
        this(null);
    }

    public ExtendableObjectParser(Grouper grouper) {
        super(ExtendableObjectParser.class.getSimpleName(), "ExtendableObject", grouper,
                ARG_NAMES[0], ARGS_DEF);
    }

    @Override
    public Response<Object> tryEvaluateObject(Map<String, Object> args) {
        // Takes object so won't every fail to cast
        return Response.is(ExtendableObject.create(args.get(ARG_NAMES[0])));
    }
}
