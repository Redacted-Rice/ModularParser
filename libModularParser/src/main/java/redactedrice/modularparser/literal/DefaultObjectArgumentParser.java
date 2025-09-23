package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.Response;

public class DefaultObjectArgumentParser implements ArgumentParser {

    @Override
    public Response<Object> parseArgument(String argument) {
        return Response.notHandled();
    }

    @Override
    public boolean isExpectedType(Object argument) {
        return true;
    }

    @Override
    public String getExpectedTypeName() {
        return "Object";
    }

}
