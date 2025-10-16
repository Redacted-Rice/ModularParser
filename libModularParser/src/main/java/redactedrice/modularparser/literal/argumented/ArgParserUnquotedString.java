package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public class ArgParserUnquotedString extends ArgParserTyped<String> {
    public ArgParserUnquotedString(boolean allowNull) {
        super(String.class, allowNull);
    }

    public ArgParserUnquotedString() {
        this(false);
    }

    @Override
    protected Response<Object> modifyMatchingResponse(Response<Object> matchingType) {
        // Type will already have been checked before calling
        return Response.is(ArgumentUtils.unquoteString((String) matchingType.getValue()));
    }

    @Override
    public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
        return Response.is(ArgumentUtils.getUnquotedString(parsed, argument));
    }
}
