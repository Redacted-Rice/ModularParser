package redactedrice.modularparser.core;


public class TestModule implements Module {

    @Override
    public String getName() {
        return "TestModule";
    }

    @Override
    public void setParser(ModularParser parser) { /* not needed for testing */}

    @Override
    public void setModuleRefs() { /* not needed for testing */}

    @Override
    public boolean checkModulesCompatibility() {
        return true;
    }
}
