package redactedrice.modularparser.core;

public class TestModule implements Module {

	@Override
	public String getName() {
		return "TestModule";
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
