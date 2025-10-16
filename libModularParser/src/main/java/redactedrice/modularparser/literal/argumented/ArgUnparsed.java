package redactedrice.modularparser.literal.argumented;


// TODO: Instead just implement argparser and return the arg always
public class ArgUnparsed extends ArgParserTyped<String> {
    public ArgUnparsed(boolean allowNull) {
        super(String.class, allowNull);
    }

    public ArgUnparsed() {
        this(false);
    }
}
