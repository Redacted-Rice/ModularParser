package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BaseSupporterTests {
    private class BaseSupporterTester extends BaseSupporter<TestModule> {
        protected BaseSupporterTester(String name, Class<TestModule> tClass) {
            super(name, tClass);
        }
    }

    final String OBJ_NAME = "Test name";

    @Test
    void constructorSetterTest() {
        BaseSupporterTester testee = new BaseSupporterTester(OBJ_NAME, TestModule.class);
        assertEquals(OBJ_NAME, testee.getName());
    }

    @Test
    void handleModuleTests() {
        TestModule module = new TestModule();
        TestModule2 module2 = new TestModule2();

        BaseSupporterTester testee = new BaseSupporterTester(OBJ_NAME, TestModule.class);
        testee.handleModule(module);
        testee.handleModule(module2);

        assertEquals(1, testee.submodules.size());
        assertEquals(module, testee.submodules.get(0));
    }
}
