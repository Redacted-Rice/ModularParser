package redactedrice.modularparser.testsupport;


import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.argumented.ArgumentParserSingleType;

public class SimpleObjectArgumentParser extends ArgumentParserSingleType {

    public SimpleObjectArgumentParser() {
        super(true);
    }

    @Override
    public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
        return Response.is(new SimpleObject(42, false, argument, null));
    }

    @Override
    protected Class<?> expectedType() {
        return SimpleObject.class;
    }
}
