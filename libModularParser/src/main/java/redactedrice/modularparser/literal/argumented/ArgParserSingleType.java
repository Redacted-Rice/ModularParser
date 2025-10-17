package redactedrice.modularparser.literal.argumented;


import java.util.Collection;
import java.util.List;

public abstract class ArgParserSingleType extends ArgParserMultiType {

    protected ArgParserSingleType(boolean allowNull) {
        super(allowNull);
    }

    public Collection<Class<?>> expectedTypes() {
        return List.of(expectedType());
    }

    public abstract Class<?> expectedType();
}
