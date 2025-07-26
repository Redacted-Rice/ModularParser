package redactedrice.modularparser.core;


public interface Module {
    String getName();

    public void setModuleRefs();

    public boolean checkModulesCompatibility();
}
