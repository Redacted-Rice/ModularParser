package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

class ModularParserTests {

    static final String LOGGER_NAME = "TestLogger";
    static final String PLAIN_MODULE_NAME = "TestModule3";
    static final String PLAIN_MODULE_2_NAME = "TestModule2";
    static final String PLAIN_SUPPORTER_NAME = "TestSupporter";
    static final String PLAIN_SUPPORTER_2_NAME = "TestSupporter2";
    static final String LFS_NAME = "TestLineFormer";
    static final String LPS_NAME = "TestLineParser";

    private interface NonCriticalSupporter extends Supporter {}

    private interface NonCriticalSupporterExt extends NonCriticalSupporter {}

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

        assertEquals(1, testee.index.size());
        assertTrue(testee.index.containsKey(LOGGER_NAME));
        assertEquals(logger, testee.index.get(LOGGER_NAME));
        assertEquals(1, testee.modulesOrdered.size());
        assertEquals(logger, testee.modulesOrdered.get(0));

        // Test readding
        when(logger.getName()).thenReturn(LOGGER_NAME + "2");
        assertFalse(testee.addModule(logger));
        assertEquals(1, testee.index.size());
        assertEquals(1, testee.modulesOrdered.size());
    }

    @Test
    void addModuleTest() {
        testee.addModule(logger);

        Module module = mock(Module.class);
        when(module.getName()).thenReturn(PLAIN_MODULE_NAME);
        assertTrue(testee.addModule(module));
        verify(logger).handleModule(module);

        assertEquals(2, testee.index.size());
        assertEquals(2, testee.modulesOrdered.size());
        assertEquals(logger, testee.modulesOrdered.get(0));
        assertEquals(module, testee.modulesOrdered.get(1));

        // Readd the module with the same name
        assertFalse(testee.addModule(module));
        assertEquals(2, testee.index.size());
        assertEquals(2, testee.modulesOrdered.size());
    }

    @Test
    void addSupporterTest() {
        testee.addModule(logger);

        NonCriticalSupporter supporter = mock(NonCriticalSupporter.class);
        when(supporter.getName()).thenReturn(PLAIN_SUPPORTER_NAME);
        assertTrue(testee.addModule(supporter));
        verify(logger).handleModule(supporter);

        assertEquals(2, testee.index.size());
        assertEquals(2, testee.modulesOrdered.size());
        assertEquals(logger, testee.modulesOrdered.get(0));
        assertEquals(supporter, testee.modulesOrdered.get(1));

        // Readd the module with the same name
        when(supporter.getName()).thenReturn(PLAIN_SUPPORTER_2_NAME);
        assertFalse(testee.addModule(supporter));
        assertEquals(2, testee.index.size());
        assertEquals(2, testee.modulesOrdered.size());
    }

    @Test
    void addCriticalSupportersTest() {
        assertTrue(testee.addModule(logger));

        LineParserSupporter lps = mock(LineParserSupporter.class);
        when(lps.getName()).thenReturn(LPS_NAME);
        assertTrue(testee.addModule(lps));
        verify(logger).handleModule(lps);
        verify(lps).handleModule(logger);

        LineFormerSupporter lfs = mock(LineFormerSupporter.class);
        when(lfs.getName()).thenReturn(LFS_NAME);
        assertTrue(testee.addModule(lfs));
        verify(logger).handleModule(lfs);
        verify(lps).handleModule(lfs);
        verify(lfs).handleModule(logger);

        assertEquals(3, testee.index.size());
        assertEquals(3, testee.modulesOrdered.size());
        assertEquals(logger, testee.modulesOrdered.get(0));
        assertEquals(lps, testee.modulesOrdered.get(1));
        assertEquals(lfs, testee.modulesOrdered.get(2));

        when(lps.getName()).thenReturn(LPS_NAME + "2");
        assertFalse(testee.addModule(lps));
        when(lfs.getName()).thenReturn(LFS_NAME + "2");
        assertFalse(testee.addModule(lfs));

        assertEquals(3, testee.index.size());
        assertEquals(3, testee.modulesOrdered.size());
    }

    @Test
    void parseTest() {
        assertTrue(testee.addModule(logger));

        // No line former or parser
        final String NO_FORMER = "ModularParser: No Line Former was added";
        final String NO_PARSER = "ModularParser: No Line Parser was added";
        final String ABORTED = "ModularParser: Aborted! See previous logs for details";

        assertFalse(testee.parse());
        assertEquals(ModularParser.Status.ABORT, testee.status);
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
        when(lfs.getNextLogicalLine()).thenReturn("test line", "   ", "test line 2", null);
        assertTrue(testee.parse());
        verify(lps, times(2)).parseLine(any());

        // Test a parser error
        final String ERRORED = "ModularParser: Failed to parser some lines! See previous logs for details";
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
        assertEquals(LineParserSupporter.class.getSimpleName(),
                testee.getSupporterInterfaceName(lps));

        Supporter supporter = mock(Supporter.class);
        assertEquals("", testee.getSupporterInterfaceName(supporter));

        NonCriticalSupporter anotherSupporter = mock(NonCriticalSupporter.class);
        assertEquals(NonCriticalSupporter.class.getSimpleName(),
                testee.getSupporterInterfaceName(anotherSupporter));

        NonCriticalSupporterExt aNestedSupporter = mock(NonCriticalSupporterExt.class);
        assertEquals(NonCriticalSupporterExt.class.getSimpleName(),
                testee.getSupporterInterfaceName(aNestedSupporter));
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
        assertEquals(module1, mod);
        mod = testee.getModule(PLAIN_MODULE_2_NAME);
        assertEquals(module2, mod);

        List<Module> mods = testee.getModulesOfType(Module.class);
        assertEquals(3, mods.size());

        List<TestModule> testMods = testee.getModulesOfType(TestModule.class);
        assertEquals(1, testMods.size());
        assertEquals(module3, testMods.get(0));
    }

    @Test
    void getSupporterTest() {
        NonCriticalSupporter supporter1 = mock(NonCriticalSupporter.class);
        when(supporter1.getName()).thenReturn(PLAIN_SUPPORTER_NAME);
        NonCriticalSupporterExt supporter2 = mock(NonCriticalSupporterExt.class);
        when(supporter2.getName()).thenReturn(PLAIN_SUPPORTER_2_NAME);

        testee.addModule(supporter1);
        assertNull(testee.getSupporterOfType(NonCriticalSupporterExt.class));

        testee.addModule(supporter2);
        assertEquals(supporter1, testee.getSupporterOfType(NonCriticalSupporter.class));
        assertEquals(supporter2, testee.getSupporterOfType(NonCriticalSupporterExt.class));
    }
}
