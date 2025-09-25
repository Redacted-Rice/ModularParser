package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public class TypeEnforcerNonNullable<T> extends TypeEnforcer<T> {

    public TypeEnforcerNonNullable(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public Response<Object> parseArgument(String argument) {
        return Response.notHandled();
    }

    @Override
    public boolean isExpectedType(Object argument) {
        return clazz.isInstance(argument);
    }
}
