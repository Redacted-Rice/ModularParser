package redactedrice.modularparser.literal.argumented;


public class TypeAny extends TypeEnforcer<Object> {
    public TypeAny(boolean allowNull) {
        super(Object.class, allowNull);
    }
    
    public TypeAny() {
        this(false);
    }
}
