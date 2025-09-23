package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.Response;

public interface ArgumentParser {
    public Response<Object> parseArgument(String argument);

    public boolean isExpectedType(Object argument);

    public String getExpectedTypeName();
}
