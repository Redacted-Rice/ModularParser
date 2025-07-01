package redactedrice.modularparser.basic;


import redactedrice.modularparser.Parser;
import redactedrice.modularparser.Module;

/** A named DSL‐line handler carrying a back‐pointer to its parser. */
public abstract class BaseModule implements Module {
    private final String name;
    protected Parser parser;

    protected BaseModule(String name, String... reservedWords) {
        this.name = name;
    }

    /** The unique name you gave this handler. */
    public String getName() {
        return name;
    }

	@Override
    public void setParser(Parser parser) {
    	this.parser = parser;
    }
}