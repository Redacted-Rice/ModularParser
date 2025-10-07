package redactedrice.modularparser.literal.argumented;


import java.util.Collection;
import java.util.List;

public abstract class ArgumentParserSingleType extends ArgumentParserMultiType {

    protected ArgumentParserSingleType(boolean allowNull) {
        super(allowNull);
    }

    protected Collection<Class<?>> expectedTypes() {
        return List.of(expectedType());
    }

    protected abstract Class<?> expectedType();
}
