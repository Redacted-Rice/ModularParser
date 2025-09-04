package redactedrice.modularparser.core;


public class TestModule2 implements Module {

    @Override
    public String getName() {
        return "TestModule2";
    }

    @Override
    public void setParser(ModularParser parser) {}

    @Override
    public void setModuleRefs() {}

    @Override
    public boolean checkModulesCompatibility() {
        return true;
    }
}
