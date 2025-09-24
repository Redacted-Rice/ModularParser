package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public class TypeEnforcer<T> implements ArgumentParser {
    protected final Class<T> clazz;

    public TypeEnforcer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Response<Object> parseArgument(String argument) {
        if (argument == null || argument.isBlank() || argument.equalsIgnoreCase("null")) {
            return Response.is(null);
        }
        return Response.notHandled();
    }

    @Override
    public boolean isExpectedType(Object argument) {
        return argument == null || clazz.isInstance(argument);
    }

    @Override
    public String getExpectedTypeName() {
        return clazz.getSimpleName();
    }
}
