package redactedrice.modularparser.testsupport;


import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.argumented.ArgumentParserBase;

public class SimpleObjectArgumentParser extends ArgumentParserBase<SimpleObject> {

    public SimpleObjectArgumentParser() {
    	super(SimpleObject.class, true);
    }

	@Override
	public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
		return Response.is(new SimpleObject(42, false, argument, null));
	}
}
