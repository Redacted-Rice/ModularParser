package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

class ModularParserModuleTests {

    final String LOGGER_NAME = "TestLogger";
    final String PLAIN_MODULE_NAME = "TestModule3";
    final String PLAIN_MODULE_2_NAME = "TestModule2";
    final String PLAIN_SUPPORTER_NAME = "TestSupporter";
    final String PLAIN_SUPPORTER_2_NAME = "TestSupporter2";
    final String LFS_NAME = "TestLineFormer";
    final String LPS_NAME = "TestLineParser";

    private interface NonCriticalSupporter extends Supporter {};

    private interface NonCriticalSupporterExt extends NonCriticalSupporter {};

    private LogSupporter logger;
    private ModularParser testee;

    @BeforeEach
    void setup() {
        testee = new ModularParser();
        logger = mock(LogSupporter.class);
        when(logger.getName()).thenReturn(LOGGER_NAME);
    }

    @Test
    void addLoggerTest() {
        assertNull(testee.getLogger());

        assertTrue(testee.addModule(logger));
        assertEquals(testee.getLogger(), logger);

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
        when(supporter.getName()).thenReturn(PLAIN_SUPPORTER_2_NAME);
        assertFalse(testee.addModule(supporter));
        assertEquals(testee.index.size(), 2);
        assertEquals(testee.modulesOrdered.size(), 2);
    }

    @Test
    void addCriticalSupportersTest() {
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
    void parseTest() {
        assertTrue(testee.addModule(logger));

        // No line former or parser
        final String NO_FORMER = "ModularParser: No Line Former was added";
        final String NO_PARSER = "ModularParser: No Line Parser was added";
        final String ABORTED = "ModularParser: Aborted! See previous logs for details";
        when(logger.format(contains("No Line Former"))).thenReturn(NO_FORMER);
        when(logger.format(contains("No Line Parser"))).thenReturn(NO_PARSER);
        when(logger.format(contains("Aborted"))).thenReturn(ABORTED);

        assertFalse(testee.parse());
        assertEquals(testee.status, ModularParser.Status.ABORT);
        verify(logger).log(LogLevel.ABORT, NO_FORMER);
        verify(logger).log(LogLevel.ABORT, NO_PARSER);
        verify(logger).log(LogLevel.ERROR, ABORTED);

        // Add line former & -arser
        testee.status = ModularParser.Status.OK;
        LineFormerSupporter lfs = mock(LineFormerSupporter.class);
        when(lfs.getName()).thenReturn(LFS_NAME);
        assertTrue(testee.addModule(lfs));
        LineParserSupporter lps = mock(LineParserSupporter.class);
        when(lps.getName()).thenReturn(LPS_NAME);
        assertTrue(testee.addModule(lps));

        // Test happy case
        when(lfs.getNextLogicalLine()).thenReturn("test line", "test line 2", null);
        assertTrue(testee.parse());
        verify(lps, times(2)).parseLine(any());

        // Test a parser error
        final String ERRORED = "ModularParser: Failed to parser some lines! See previous logs for details";
        when(logger.format(contains("Failed to parser some lines"))).thenReturn(ERRORED);
        when(lfs.getNextLogicalLine()).thenReturn("test line", "test line 2", null);
        doAnswer(invocation -> {
            testee.status = ModularParser.Status.ERROR;
            return null;
        }).when(lps).parseLine(any());

        assertFalse(testee.parse());
        verify(logger).log(LogLevel.ERROR, ERRORED);
    }

    @Test
    void getSupporterInterfaceNameTest() {
        assertEquals(testee.getSupporterInterfaceName(logger), LogSupporter.class.getSimpleName());

        LineParserSupporter lps = mock(LineParserSupporter.class);
        assertEquals(testee.getSupporterInterfaceName(lps),
                LineParserSupporter.class.getSimpleName());

        Supporter supporter = mock(Supporter.class);
        assertEquals(testee.getSupporterInterfaceName(supporter), "");

        NonCriticalSupporter anotherSupporter = mock(NonCriticalSupporter.class);
        assertEquals(testee.getSupporterInterfaceName(anotherSupporter),
                NonCriticalSupporter.class.getSimpleName());

        NonCriticalSupporterExt aNestedSupporter = mock(NonCriticalSupporterExt.class);
        assertEquals(testee.getSupporterInterfaceName(aNestedSupporter),
                NonCriticalSupporterExt.class.getSimpleName());
    }

    @Test
    void configureModulesAllCompatible() {
        Module module1 = mock(Module.class);
        Module module2 = mock(Module.class);

        when(module1.checkModulesCompatibility()).thenReturn(true);
        when(module2.checkModulesCompatibility()).thenReturn(true);

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

    @Test
    void getModuleTest() {
        Module module1 = mock(Module.class);
        when(module1.getName()).thenReturn(PLAIN_MODULE_NAME);
        Module module2 = mock(Module.class);
        when(module2.getName()).thenReturn(PLAIN_MODULE_2_NAME);
        TestModule module3 = mock(TestModule.class);

        testee.modulesOrdered.add(module1);
        testee.modulesOrdered.add(module2);
        testee.modulesOrdered.add(module3);
        testee.index.put(module1.getName(), module1);
        testee.index.put(module2.getName(), module2);
        testee.index.put(module3.getName(), module3);

        Module mod = testee.getModule(PLAIN_MODULE_NAME);
        assertEquals(mod, module1);
        mod = testee.getModule(PLAIN_MODULE_2_NAME);
        assertEquals(mod, module2);

        List<Module> mods = testee.getModulesOfType(Module.class);
        assertEquals(mods.size(), 3);

        List<TestModule> testMods = testee.getModulesOfType(TestModule.class);
        assertEquals(testMods.size(), 1);
        assertEquals(testMods.get(0), module3);
    }

    @Test
    void getSupporterTest() {
        NonCriticalSupporter supporter1 = mock(NonCriticalSupporter.class);
        when(supporter1.getName()).thenReturn(PLAIN_SUPPORTER_NAME);
        NonCriticalSupporterExt supporter2 = mock(NonCriticalSupporterExt.class);
        when(supporter2.getName()).thenReturn(PLAIN_SUPPORTER_2_NAME);

        testee.addModule(supporter1);
        testee.addModule(supporter2);

        assertEquals(testee.getSupporterOfType(NonCriticalSupporter.class), supporter1);
        assertEquals(testee.getSupporterOfType(NonCriticalSupporterExt.class), supporter2);
    }
}
