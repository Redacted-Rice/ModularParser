package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public abstract class ArgumentParserBase<T> implements ArgumentParser {
	protected final boolean allowNull;
    protected final Class<T> clazz;
    
	protected ArgumentParserBase(Class<T> clazz, boolean allowNull) {
		this.allowNull = allowNull;
		this.clazz = clazz;
	}

	public Response<Object> tryParseArgument(Response<Object> parsed, String argument) {	
		if (parsed.wasValueReturned() && clazz.isInstance(parsed.getValue())) {
			return parsed;
		}
		
		if ((parsed.wasHandled() && parsed.getValue() == null) || argument.isBlank() || argument.equalsIgnoreCase("null")) {
			if (allowNull) {
				return Response.is(null);
			} else {
				return Response.error("Null value was passed but is not allowed");
			}
        }
		return tryParseNonNullArgument(parsed, argument);
	}
	
	protected abstract Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument);
}
