package redactedrice.modularparser.literal.argumented;


import java.util.Collection;

public class ArgParserMapOfCollection extends ArgParserValueTypedMapBase {
    public ArgParserMapOfCollection(boolean allowNull) {
        super(allowNull);
    }

    @Override
    protected Class<?> expectedType() {
        return Collection.class;
    }
}
