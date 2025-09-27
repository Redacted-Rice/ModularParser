package redactedrice.modularparser.literal.argumented;


public class TypeAny extends TypeEnforcer<Object> {
    public TypeAny(boolean allowNull) {
        super(allowNull, Object.class);
    }
}
