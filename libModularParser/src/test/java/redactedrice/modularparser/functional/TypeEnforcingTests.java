package redactedrice.modularparser.functional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.comment.DefaultMutliLineCommentLineModifier;
import redactedrice.modularparser.comment.DefaultSingleLineCommentLineModifier;
import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.lineformer.DefaultGroupingLineModifier;
import redactedrice.modularparser.lineformer.DefaultLineFormerSupporter;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.lineparser.DefaultLineParserSupporter;
import redactedrice.modularparser.literal.DefaultBoolLiteralParser;
import redactedrice.modularparser.literal.DefaultCharLiteralParser;
import redactedrice.modularparser.literal.DefaultLiteralSupporter;
import redactedrice.modularparser.literal.DefaultNumberLiteralParser;
import redactedrice.modularparser.log.DefaultCacheLogHandler;
import redactedrice.modularparser.log.DefaultLogSupporter;
import redactedrice.modularparser.reserved.DefaultReservedWordSupporter;
import redactedrice.modularparser.scope.DefaultScopeSupporter;
import redactedrice.modularparser.scope.DefaultScopedVarConstParser;
import redactedrice.modularparser.testsupport.SimpleObject;
import redactedrice.modularparser.testsupport.SimpleObjectTypedLiteralParser;

class TypeEnforcingTests {
    ModularParser parser;
    DefaultLineFormerSupporter reader;
    DefaultScopedVarConstParser varParser;
    DefaultScopedVarConstParser constParser;
    DefaultCacheLogHandler logger;

    @BeforeEach
    void setup() {
        parser = new ModularParser();
        reader = new DefaultLineFormerSupporter();
        parser.addModule(reader);
        parser.addModule(new DefaultLineParserSupporter());
        parser.addModule(new DefaultLiteralSupporter());
        parser.addModule(new DefaultReservedWordSupporter());
        parser.addModule(new DefaultLogSupporter());

        logger = new DefaultCacheLogHandler();
        logger.enableAll(false);
        logger.enable(LogLevel.ERROR, true);
        logger.enable(LogLevel.ABORT, true);
        parser.addModule(logger);

        parser.addModule(new DefaultSingleLineCommentLineModifier("DoubleSlashComments", "//"));
        parser.addModule(
                new DefaultMutliLineCommentLineModifier("MutlilineSlashStarComments", "/*", "*/"));
        DefaultScopeSupporter scope = new DefaultScopeSupporter(true);
        scope.pushScope("global");
        scope.pushScope("file");
        parser.addModule(scope);

        Grouper parenGrouper = new DefaultGroupingLineModifier("BasicParenthesisModule", "(", ")",
                false);
        // Don't set as static default to prevent interfering with other tests
        parser.addModule(parenGrouper);

        parser.addModule(new DefaultNumberLiteralParser());
        parser.addModule(new DefaultCharLiteralParser());
        parser.addModule(new DefaultBoolLiteralParser());
        parser.addModule(new SimpleObjectTypedLiteralParser(parenGrouper));
        varParser = new DefaultScopedVarConstParser("BasicVarHandler", true, "var");

        parser.addModule(varParser);
        constParser = new DefaultScopedVarConstParser("BasicConstHandler", false, "const");
        parser.addModule(constParser);
    }

    @Test
    void parseType_implicitType() {
        parser.configureModules();

        String script = """
        		/* basic test cases for creating variables */
                var so1 = SimpleObject(1, false, "so1")
                var so2 = SimpleObject(2, false, "so2", so3)
                """;

        // Run parser
        reader.setReader(new BufferedReader(new StringReader(script)));
        assertTrue(parser.parse());
        assertTrue(logger.getLogs().isEmpty());
        SimpleObject so1 = (SimpleObject) varParser.getVariableValue("so1").getValue();
        assertEquals(1, so1.intField);
        assertEquals("so1", so1.strField);

        SimpleObject so2 = (SimpleObject) varParser.getVariableValue("so2").getValue();
        assertEquals(2, so2.intField);
        assertEquals("so2", so2.strField);
        assertNotNull(so2.so);

        SimpleObject so3 = so2.so;
        assertEquals(42, so3.intField);
        assertEquals("so3", so3.strField);
    }

    @Test
    void badType() {
        parser.configureModules();

        String script = """
                /* basic test cases for creating variables */
                var so1 = SimpleObject("bad", false, "so1")
                """;

        // Run parser
        reader.setReader(new BufferedReader(new StringReader(script)));
        assertFalse(parser.parse());
        String logs = logger.getLogsCombined();
        assertFalse(logs.isBlank());
        assertTrue(logs.contains(
                "[ERROR] SimpleObjectTypedLiteralParser: Parsed arg intVal (\"bad\") but returned value was not the of expected type (Integer). Found obj = bad"));
    }
}