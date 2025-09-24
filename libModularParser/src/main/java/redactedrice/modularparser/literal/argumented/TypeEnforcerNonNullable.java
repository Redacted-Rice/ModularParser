package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public class TypeEnforcerNonNullable<T> implements ArgumentParser {
    protected final Class<T> clazz;

    public TypeEnforcerNonNullable(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Response<Object> parseArgument(String argument) {
        return Response.notHandled();
    }

    @Override
    public boolean isExpectedType(Object argument) {
        return clazz.isInstance(argument);
    }

    @Override
    public String getExpectedTypeName() {
        return clazz.getSimpleName();
    }
}
