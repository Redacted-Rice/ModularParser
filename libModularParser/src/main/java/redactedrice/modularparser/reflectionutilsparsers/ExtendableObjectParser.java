package redactedrice.modularparser.reflectionutilsparsers;


import java.util.Map;

import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.BaseArgumentChainableLiteral;
import redactedrice.reflectionhelpers.objects.ExtendableObject;

public class ExtendableObjectParser extends BaseArgumentChainableLiteral {
    private final static String[] argsOrdered = new String[] {"object"};

    public ExtendableObjectParser() {
        super(ExtendableObjectParser.class.getSimpleName(), "ExtendableObject", 
        		argsOrdered[0], new String[] {argsOrdered[0]},
                new String[] {}, new Object[] {});
    }

    @Override
    public Response<Object> tryEvaluateObject(Map<String, Object> args) {
    	try {
            return Response.is(ExtendableObject.create(args.get(argsOrdered[0])));
    	} catch (ClassCastException e) {
    		// This should have been handled if this was called
    	    return Response.error("failed to cast value: " + e.getMessage());
    	}
    }
}
