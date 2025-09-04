package redactedrice.modularparser.reflectionutilsparsers;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.BaseArgumentChainableLiteral;
import redactedrice.reflectionhelpers.objects.ExtendableObject;

public class ExtendableObjectParser extends BaseArgumentChainableLiteral {
    private static final String[] argsOrdered = new String[] {"object"};

    public ExtendableObjectParser() {
        super(ExtendableObjectParser.class.getSimpleName(), "ExtendableObject", argsOrdered[0],
                new String[] {argsOrdered[0]}, new String[] {}, new Object[] {});
    }

    @Override
    public Response<Object> tryEvaluateObject(Map<String, Object> args) {
        // Takes object so won't every fail to cast
        return Response.is(ExtendableObject.create(args.get(argsOrdered[0])));
    }
}
