package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class BaseSupporterTests {
    final String OBJ_NAME = "Test name";

    @Test
    void constructorSetterTest() {
        ModularParser parser = mock(ModularParser.class);
        BaseSupporterTester testee = new BaseSupporterTester(OBJ_NAME, parser, TestModule.class);
        assertEquals(testee.getName(), OBJ_NAME);
    }

    @Test
    void handleModuleTests() {
        TestModule module = new TestModule();
        TestModule2 module2 = new TestModule2();

        ModularParser parser = mock(ModularParser.class);
        BaseSupporterTester testee = new BaseSupporterTester(OBJ_NAME, parser, TestModule.class);
        testee.handleModule(module);
        testee.handleModule(module2);
        
        assertEquals(testee.submodules.size(), 1);
        assertEquals(testee.submodules.get(0), module);
    }
}
