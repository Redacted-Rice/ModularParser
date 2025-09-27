package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public class TypeEnforcer<T> extends ArgumentParserBase {
    protected final Class<T> clazz;

    public TypeEnforcer(boolean allowNull, Class<T> clazz) {
    	super(allowNull);
        this.clazz = clazz;
    }

	@Override
	public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
		if (parsed.wasValueReturned() && clazz.isInstance(parsed.getValue())) {
			return parsed;
		}
        return Response.error("Expected value of type " + clazz.getSimpleName());
	}
}
