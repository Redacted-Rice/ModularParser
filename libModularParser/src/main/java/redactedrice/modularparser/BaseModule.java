package redactedrice.modularparser;


/** A named DSL‐line handler carrying a back‐pointer to its parser. */
public abstract class BaseModule implements Module {
    private final String name;
    protected ModularParser parser;

    protected BaseModule(String name) {
        this.name = name;
    }

    /** The unique name you gave this handler. */
    public String getName() {
        return name;
    }

    @Override
    public void setParser(ModularParser parser) {
        this.parser = parser;
    }

    @Override
    public void configure() {}

    // TODO: Move to a variable support module
    protected boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
}