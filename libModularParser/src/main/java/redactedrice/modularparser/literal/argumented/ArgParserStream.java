package redactedrice.modularparser.literal.argumented;


import java.util.stream.Stream;

import redactedrice.modularparser.core.Response;
import redactedrice.reflectionhelpers.utils.ConversionUtils;

public class ArgParserStream extends ArgParserSingleType {

    public ArgParserStream(boolean allowNull) {
        super(allowNull);
    }

    @Override
    protected Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
        if (parsed.wasHandled()) {
            Stream<Object> stream = ConversionUtils.convertToStreamOrNull(parsed.getValue());
            if (stream != null) {
                return Response.is(stream);
            }
        }
        return Response.notHandled();
    }

    @Override
    public Class<?> expectedType() {
        return Stream.class;
    }
}
