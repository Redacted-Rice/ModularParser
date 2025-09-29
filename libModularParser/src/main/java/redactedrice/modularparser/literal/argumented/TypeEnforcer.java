package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public class TypeEnforcer<T> extends ArgumentParserBase {
    protected final Class<T> clazz;

    public TypeEnforcer(Class<T> clazz, boolean allowNull) {
        super(allowNull);
        this.clazz = clazz;
    }

    public TypeEnforcer(Class<T> clazz) {
        this(clazz, false);
    }

    @Override
    public Response<Object> tryParseNonNullArgument(Response<Object> parsed, String argument) {
        // Nothing else to do here
        return Response.error("Expected value of type " + clazz.getSimpleName());
    }

    @Override
    protected Class<?> expectedType() {
        return clazz;
    }
}
