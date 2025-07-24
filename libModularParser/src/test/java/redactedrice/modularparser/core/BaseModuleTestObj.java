package redactedrice.modularparser.core;


public class BaseModuleTestObj extends BaseModule {
    public BaseModuleTestObj(String name) {
        super(name);
    }

    public ModularParser getModularParser() {
        return parser;
    }
}
