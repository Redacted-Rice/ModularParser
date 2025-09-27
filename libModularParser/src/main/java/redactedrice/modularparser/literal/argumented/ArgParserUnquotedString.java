package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public class ArgParserUnquotedString extends TypeEnforcer<String> {
    public ArgParserUnquotedString(boolean allowNull) {
        super(allowNull, String.class);
    }

	@Override
	public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
		return Response.is(ArgumentUtils.getUnquotedString(parsed, argument));
	}
}
