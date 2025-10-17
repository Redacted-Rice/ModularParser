package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public class ArgUnparsed implements ArgumentParser {
    @Override
    public Response<Object> preparseEvaluate(String argument) {
        return Response.is(argument);
    }

    @Override
    public Response<Object> tryParseArgument(Response<Object> parsed, String argument) {
        return Response.error("Not expected to get here!");
    }
}
