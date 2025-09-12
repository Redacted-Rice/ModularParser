package redactedrice.modularparser.functional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import redactedrice.modularparser.literal.DefaultChainingChainableLiteralParser;
import redactedrice.modularparser.literal.DefaultCharLiteralParser;
import redactedrice.modularparser.literal.DefaultLiteralSupporter;
import redactedrice.modularparser.literal.DefaultNumberLiteralParser;
import redactedrice.modularparser.literal.DefaultObjectPathParser;
import redactedrice.modularparser.log.DefaultCacheLogHandler;
import redactedrice.modularparser.reflectionutilsparsers.ExtendableObjectParser;
import redactedrice.modularparser.reserved.DefaultReservedWordSupporter;
import redactedrice.modularparser.scope.DefaultScopeSupporter;
import redactedrice.modularparser.scope.DefaultScopedVarConstParser;
import redactedrice.modularparser.testsupport.SimpleObject;
import redactedrice.modularparser.testsupport.SimpleObjectLiteralParser;
import redactedrice.reflectionhelpers.objects.ExtendableObject;

class ObjectCreationAndChainingTests {

    ModularParser parser;
    DefaultLineFormerSupporter reader;
    DefaultScopedVarConstParser varParser;
    DefaultCacheLogHandler logger;

    @BeforeEach
    void setup() {
        parser = new ModularParser();
        reader = new DefaultLineFormerSupporter();
        parser.addModule(reader);
        parser.addModule(new DefaultLineParserSupporter());
        parser.addModule(new DefaultLiteralSupporter());
        parser.addModule(new DefaultReservedWordSupporter());

        logger = new DefaultCacheLogHandler();
        logger.enableAll(false);
        logger.enable(LogLevel.ERROR, true);
        logger.enable(LogLevel.ABORT, true);
        parser.addModule(logger);

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
        parser.addModule(new SimpleObjectLiteralParser(parenGrouper));
        parser.addModule(new ExtendableObjectParser(parenGrouper));
        varParser = new DefaultScopedVarConstParser("BasicVarHandler", true, "var");
        parser.addModule(varParser);

        parser.addModule(new DefaultSingleLineCommentLineModifier("DoubleSlashComments", "//"));
        parser.addModule(
                new DefaultMutliLineCommentLineModifier("MutlilineSlashStarComments", "/*", "*/"));
    }

    @Test
    void basicConstructors() {
        parser.configureModules();

        String script = """
                var so = SimpleObject(1, true, "so1")
                var eo = ExtendableObject("5")
                """;

        // Run parser
        reader.setReader(new BufferedReader(new StringReader(script)));
        assertTrue(parser.parse());
        assertTrue(logger.getLogs().isEmpty());

        assertTrue(varParser.isVariable("so"));
        SimpleObject so = (SimpleObject) varParser.getVariableValue("so").getValue();
        assertEquals(1, so.intField);
        assertTrue(so.boolField);
        assertEquals("so1", so.strField);

        assertTrue(varParser.isVariable("eo"));
        ExtendableObject eo = (ExtendableObject) varParser.getVariableValue("eo").getValue();
        assertEquals("5", eo.getObject());
    }

    @Test
    void chainedConstructors() {
        parser.addModule(new DefaultChainingChainableLiteralParser("BasicStackArrowChainer", "<-",
                false, parser));
        parser.addModule(new DefaultChainingChainableLiteralParser("BasicQueueArrowChainer", "->",
                true, parser));
        parser.configureModules();

        String script = """
                var obj3 = SimpleObject(1, false, "so1") ->
                    SimpleObject(2, false, "so2") -> SimpleObject(3, false, "so3") 
                var obj4 = SimpleObject(4, false, "so4") <-
                    SimpleObject(5, false, "so5") <- SimpleObject(6, false, "so6") 
                var eo = ExtendableObject() <- SimpleObject(7, true, "so7")
                """;

        // Run parser
        reader.setReader(new BufferedReader(new StringReader(script)));
        assertTrue(parser.parse());
        assertTrue(logger.getLogs().isEmpty());

        assertTrue(varParser.isVariable("obj3"));
        SimpleObject obj3 = (SimpleObject) varParser.getVariableValue("obj3").getValue();
        assertEquals(3, obj3.intField);
        assertEquals(2, obj3.so.intField);
        assertEquals(1, obj3.so.so.intField);

        assertTrue(varParser.isVariable("obj4"));
        SimpleObject obj4 = (SimpleObject) varParser.getVariableValue("obj4").getValue();
        assertEquals(4, obj4.intField);
        assertEquals(5, obj4.so.intField);
        assertEquals(6, obj4.so.so.intField);

        assertTrue(varParser.isVariable("eo"));
        ExtendableObject eo = (ExtendableObject) varParser.getVariableValue("eo").getValue();
        assertEquals(7, ((SimpleObject) eo.getObject()).intField);
    }

    @Test
    void chainedPath() {
        parser.addModule(new DefaultChainingChainableLiteralParser("BasicQueueArrowChainer", "->",
                true, parser));
        parser.addModule(new DefaultObjectPathParser("BasicOjectPathParser", ".", ",", parser));
        parser.configureModules();

        String script = """
                var obj3 = SimpleObject(1, true, "so1") ->
                    SimpleObject(2, false, "so2") -> SimpleObject(3, false, "so3") 
                var intField = obj3.so.so.intField 
                var boolField = obj3.getSo().getSo().getBool() 
                obj3.getSo().getSo().setInt(5) 
                """;

        // Run parser
        reader.setReader(new BufferedReader(new StringReader(script)));
        assertTrue(parser.parse());
        assertTrue(logger.getLogs().isEmpty());

        assertTrue(varParser.isVariable("obj3"));
        SimpleObject obj3 = (SimpleObject) varParser.getVariableValue("obj3").getValue();
        assertEquals(3, obj3.intField);
        assertEquals(2, obj3.so.intField);
        assertEquals(5, obj3.so.so.intField);

        assertTrue(varParser.isVariable("intField"));
        int intField = (int) varParser.getVariableValue("intField").getValue();
        assertEquals(1, intField);

        assertTrue(varParser.isVariable("boolField"));
        boolean boolField = (boolean) varParser.getVariableValue("boolField").getValue();
        assertTrue(boolField);
    }

    @Test
    void nestedConstructors() {
        parser.configureModules();

        String script = """
                var so1 = SimpleObject(1, true, "so1", SimpleObject(2, true, "so2", SimpleObject(3, true, "so3")))
                """;

        // Run parser
        reader.setReader(new BufferedReader(new StringReader(script)));
        assertTrue(parser.parse());
        assertTrue(logger.getLogs().isEmpty());

        assertTrue(varParser.isVariable("so1"));
        SimpleObject so1 = (SimpleObject) varParser.getVariableValue("so1").getValue();
        assertEquals(1, so1.intField);
        assertTrue(so1.boolField);
        assertEquals("so1", so1.strField);
        assertEquals(2, so1.so.intField);
        assertEquals(3, so1.so.so.intField);
        assertNull(so1.so.so.so);
    }

    @Test
    void mixedChaining() {
        parser.addModule(new DefaultChainingChainableLiteralParser("BasicStackArrowChainer", "<-",
                false, parser));
        parser.addModule(new DefaultChainingChainableLiteralParser("BasicQueueArrowChainer", "->",
                true, parser));
        parser.addModule(new DefaultObjectPathParser("BasicOjectPathParser", ".", ",", parser));
        parser.configureModules();

        String script = """
                var intVal = SimpleObject(1).intField
                var intVal2 = SimpleObject(2) -> SimpleObject(3).getSo().intField
                
                /* Processed as ExtendableObject(4) <- [ [ SimpleObject(5, so [ SimpleObject(4) ]).getSo() ] -> SimpleObject(6) ]
                 * So 4 is created and passed to 5. 4 is then pulled from 5 and passed to 6. 6 is then passed to eo
                 * So the result is an eo with so 6 that contains so 4 */
                var eo = ExtendableObject() <- SimpleObject(5, so SimpleObject(4)).getSo() -> SimpleObject(6)
                
                /* this is another case that seems odd but does work.
                 * since <- is added first, it is processed first. This then means that this becomes equivalent to
                 * SimpleObject(2) -> SimpleObject(1) -> ExtendableObject() */
                var eo2 = SimpleObject(1) -> ExtendableObject() <- SimpleObject(2)
                """;
        // Run parser
        reader.setReader(new BufferedReader(new StringReader(script)));
        assertTrue(parser.parse());
        assertTrue(logger.getLogs().isEmpty());

        assertTrue(varParser.isVariable("intVal"));
        assertEquals(1, varParser.getVariableValue("intVal").getValue());
        assertTrue(varParser.isVariable("intVal2"));
        assertEquals(2, varParser.getVariableValue("intVal2").getValue());
        assertTrue(varParser.isVariable("eo"));
        SimpleObject eoSo = (SimpleObject) ((ExtendableObject) varParser.getVariableValue("eo")
                .getValue()).getObject();
        assertEquals(6, eoSo.getInt());
        assertEquals(4, eoSo.getSo().getInt());
        assertTrue(varParser.isVariable("eo2"));
        SimpleObject eo2So = (SimpleObject) ((ExtendableObject) varParser.getVariableValue("eo2")
                .getValue()).getObject();
        assertEquals(1, eo2So.getInt());
        assertEquals(2, eo2So.getSo().getInt());
    }

    @Test
    void mixedChaining_orderMattersSome() {
        parser.addModule(new DefaultChainingChainableLiteralParser("BasicQueueArrowChainer", "->",
                true, parser));
        parser.addModule(new DefaultChainingChainableLiteralParser("BasicStackArrowChainer", "<-",
                false, parser));
        parser.configureModules();

        String script = """
                // If we add them in a different order, we get a different result
                // ordered added is effectively operator precedence. In thise case it
                // become effectively
                // ExtendableObject() <- SimpleObject(2) <- SimpleObject(1) 
                var eo2 = SimpleObject(1) -> ExtendableObject() <- SimpleObject(2)
                """;
        // Run parser
        reader.setReader(new BufferedReader(new StringReader(script)));
        assertTrue(parser.parse());
        assertTrue(logger.getLogs().isEmpty());
        SimpleObject eo2So = (SimpleObject) ((ExtendableObject) varParser.getVariableValue("eo2")
                .getValue()).getObject();
        assertEquals(2, eo2So.getInt());
        assertEquals(1, eo2So.getSo().getInt());
    }
}
