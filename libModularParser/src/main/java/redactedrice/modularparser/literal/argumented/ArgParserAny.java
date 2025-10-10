package redactedrice.modularparser.literal.argumented;


public class ArgParserAny extends ArgParserTyped<Object> {
    public ArgParserAny(boolean allowNull) {
        super(Object.class, allowNull);
    }
    
    public ArgParserAny() {
        this(false);
    }
}
