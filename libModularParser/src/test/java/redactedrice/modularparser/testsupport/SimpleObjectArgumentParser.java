package redactedrice.modularparser.testsupport;


import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.ArgumentParser;

public class SimpleObjectArgumentParser implements ArgumentParser {

    @Override
    public Response<Object> parseArgument(String argument) {
        return Response.is(new SimpleObject(1, false, argument, null));
    }

    @Override
    public boolean isExpectedType(Object argument) {
        return argument instanceof SimpleObject;
    }

    @Override
    public String getExpectedTypeName() {
        return SimpleObject.class.getSimpleName();
    }
}
