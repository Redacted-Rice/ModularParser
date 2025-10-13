package redactedrice.modularparser.testsupport;


import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.argumented.ArgParserSingleType;

public class SimpleObjectArgumentParser extends ArgParserSingleType {

    public SimpleObjectArgumentParser() {
        super(true);
    }

    @Override
    public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
        if (argument.equals("unhandledTest")) {
            return Response.notHandled();
        }
        return Response.is(new SimpleObject(42, false, argument, null));
    }

    @Override
    protected Class<?> expectedType() {
        return SimpleObject.class;
    }
}
