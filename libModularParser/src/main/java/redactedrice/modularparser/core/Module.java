package redactedrice.modularparser.core;


public interface Module {
    String getName();

    void setParser(ModularParser parser);

    public void setModuleRefs();

    public boolean checkModulesCompatibility();
}
