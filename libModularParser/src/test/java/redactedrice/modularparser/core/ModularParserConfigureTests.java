package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class ModularParserConfigureTests {

    final String LOGGER_NAME = "TestLogger";
    final String PLAIN_MODULE_NAME = "TestModule";
    final String PLAIN_SUPPORTER_NAME = "TestSupporter";
    final String LFS_NAME = "TestLineFormer";
    final String LPS_NAME = "TestLineParser";

    private interface NonCriticalSupporter extends Supporter {};

    private interface NonCriticalSupporterExt extends NonCriticalSupporter {};

    @Test
    void addLoggerTest() {
        ModularParser testee = new ModularParser();
        LogSupporter logger = mock(LogSupporter.class);
        when(logger.getName()).thenReturn(LOGGER_NAME);

        assertTrue(testee.addModule(logger));

        assertEquals(testee.index.size(), 1);
        assertTrue(testee.index.containsKey(LOGGER_NAME));
        assertEquals(testee.index.get(LOGGER_NAME), logger);
        assertEquals(testee.modulesOrdered.size(), 1);
        assertEquals(testee.modulesOrdered.get(0), logger);

        // Test readding
        when(logger.getName()).thenReturn(LOGGER_NAME + "2");
        assertFalse(testee.addModule(logger));
        assertEquals(testee.index.size(), 1);
        assertEquals(testee.modulesOrdered.size(), 1);
    }

    @Test
    void addModuleTest() {
        ModularParser testee = new ModularParser();
        LogSupporter logger = mock(LogSupporter.class);
        when(logger.getName()).thenReturn(LOGGER_NAME);
        testee.addModule(logger);

        Module module = mock(Module.class);
        when(module.getName()).thenReturn(PLAIN_MODULE_NAME);
        assertTrue(testee.addModule(module));
        verify(logger).handleModule(eq(module));

        assertEquals(testee.index.size(), 2);
        assertEquals(testee.modulesOrdered.size(), 2);
        assertEquals(testee.modulesOrdered.get(0), logger);
        assertEquals(testee.modulesOrdered.get(1), module);

        // Readd the module with the same name
        assertFalse(testee.addModule(module));
        assertEquals(testee.index.size(), 2);
        assertEquals(testee.modulesOrdered.size(), 2);
    }

    @Test
    void addSupporterTest() {
        ModularParser testee = new ModularParser();
        LogSupporter logger = mock(LogSupporter.class);
        when(logger.getName()).thenReturn(LOGGER_NAME);
        testee.addModule(logger);

        NonCriticalSupporter supporter = mock(NonCriticalSupporter.class);
        when(supporter.getName()).thenReturn(PLAIN_SUPPORTER_NAME);
        assertTrue(testee.addModule(supporter));
        verify(logger).handleModule(eq(supporter));

        assertEquals(testee.index.size(), 2);
        assertEquals(testee.modulesOrdered.size(), 2);
        assertEquals(testee.modulesOrdered.get(0), logger);
        assertEquals(testee.modulesOrdered.get(1), supporter);

        // Readd the module with the same name
        when(supporter.getName()).thenReturn(PLAIN_SUPPORTER_NAME + "2");
        assertFalse(testee.addModule(supporter));
        assertEquals(testee.index.size(), 2);
        assertEquals(testee.modulesOrdered.size(), 2);
    }

    @Test
    void addCriticalSupportersTest() {
        ModularParser testee = new ModularParser();
        LogSupporter logger = mock(LogSupporter.class);
        when(logger.getName()).thenReturn(LOGGER_NAME);
        assertTrue(testee.addModule(logger));

        LineParserSupporter lps = mock(LineParserSupporter.class);
        when(lps.getName()).thenReturn(LPS_NAME);
        assertTrue(testee.addModule(lps));
        verify(logger).handleModule(eq(lps));
        verify(lps).handleModule(eq(logger));

        LineFormerSupporter lfs = mock(LineFormerSupporter.class);
        when(lfs.getName()).thenReturn(LFS_NAME);
        assertTrue(testee.addModule(lfs));
        verify(logger).handleModule(eq(lfs));
        verify(lps).handleModule(eq(lfs));
        verify(lfs).handleModule(eq(logger));

        assertEquals(testee.index.size(), 3);
        assertEquals(testee.modulesOrdered.size(), 3);
        assertEquals(testee.modulesOrdered.get(0), logger);
        assertEquals(testee.modulesOrdered.get(1), lps);
        assertEquals(testee.modulesOrdered.get(2), lfs);

        when(lps.getName()).thenReturn(LPS_NAME + "2");
        assertFalse(testee.addModule(lps));
        when(lfs.getName()).thenReturn(LFS_NAME + "2");
        assertFalse(testee.addModule(lfs));

        assertEquals(testee.index.size(), 3);
        assertEquals(testee.modulesOrdered.size(), 3);
    }

    @Test
    void getSupporterInterfaceNameTest() {
        ModularParser testee = new ModularParser();
        LogSupporter logger = mock(LogSupporter.class);
        assertEquals(testee.getSupporterInterfaceName(logger), LogSupporter.class.getSimpleName());

        LineParserSupporter lps = mock(LineParserSupporter.class);
        assertEquals(testee.getSupporterInterfaceName(lps),
                LineParserSupporter.class.getSimpleName());

        Supporter supporter = mock(Supporter.class);
        assertEquals(testee.getSupporterInterfaceName(supporter), "");

        NonCriticalSupporter anotherSupporter = mock(NonCriticalSupporter.class);
        assertEquals(testee.getSupporterInterfaceName(anotherSupporter),
                NonCriticalSupporter.class.getSimpleName());

        NonCriticalSupporter aNestedSupporter = mock(NonCriticalSupporterExt.class);
        assertEquals(testee.getSupporterInterfaceName(aNestedSupporter),
                NonCriticalSupporterExt.class.getSimpleName());
    }

    @Test
    void configureModulesAllCompatible() {
        Module module1 = mock(Module.class);
        Module module2 = mock(Module.class);

        when(module1.checkModulesCompatibility()).thenReturn(true);
        when(module2.checkModulesCompatibility()).thenReturn(true);

        ModularParser testee = new ModularParser();
        testee.modulesOrdered.add(module1);
        testee.modulesOrdered.add(module2);

        assertTrue(testee.configureModules());

        verify(module1).setModuleRefs();
        verify(module2).setModuleRefs();
        verify(module1).checkModulesCompatibility();
        verify(module2).checkModulesCompatibility();
    }

    @Test
    void configureModulesSomeIncompatible() {
        Module goodModule = mock(Module.class);
        Module badModule1 = mock(Module.class);
        Module badModule2 = mock(Module.class);

        when(goodModule.checkModulesCompatibility()).thenReturn(true);
        when(badModule1.checkModulesCompatibility()).thenReturn(false);
        when(badModule1.getName()).thenReturn("BadModule1");
        when(badModule2.checkModulesCompatibility()).thenReturn(false);
        when(badModule2.getName()).thenReturn("BadModule2");

        ModularParser testee = spy(new ModularParser());
        testee.modulesOrdered.add(goodModule);
        testee.modulesOrdered.add(badModule1);
        testee.modulesOrdered.add(badModule2);

        assertFalse(testee.configureModules());

        verify(goodModule).setModuleRefs();
        verify(badModule1).setModuleRefs();
        verify(badModule2).setModuleRefs();
        verify(badModule1).checkModulesCompatibility();
        verify(badModule2).checkModulesCompatibility();
    }
}
