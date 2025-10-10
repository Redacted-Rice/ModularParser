package redactedrice.modularparser.literal.argumented;


public class ArgUnparsed extends ArgParserTyped<String> {
    public ArgUnparsed(boolean allowNull) {
        super(String.class, allowNull);
    }

    public ArgUnparsed() {
        this(false);
    }
}
