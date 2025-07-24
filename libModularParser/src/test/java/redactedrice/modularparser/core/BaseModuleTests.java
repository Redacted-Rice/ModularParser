package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BaseModuleTests {
    @Test
    void simple() {
        final String OBJ_NAME = "Test name";
        BaseModule testee = new BaseModuleTestObj(OBJ_NAME);
        assertEquals(testee.getName(), OBJ_NAME);
    }
}
