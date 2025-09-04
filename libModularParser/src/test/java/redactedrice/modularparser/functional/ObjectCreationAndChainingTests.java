package redactedrice.modularparser.functional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.lineformer.DefaultGroupingLineModifier;
import redactedrice.modularparser.lineformer.DefaultLineFormerSupporter;
import redactedrice.modularparser.lineparser.DefaultLineParserSupporter;
import redactedrice.modularparser.literal.DefaultBoolLiteralParser;
import redactedrice.modularparser.literal.DefaultChainingChainableLiteralParser;
import redactedrice.modularparser.literal.DefaultCharLiteralParser;
import redactedrice.modularparser.literal.DefaultLiteralSupporter;
import redactedrice.modularparser.literal.DefaultNumberLiteralParser;
import redactedrice.modularparser.literal.DefaultObjectPathParser;
import redactedrice.modularparser.log.DefaultConsoleLogHandler;
import redactedrice.modularparser.reflectionutilsparsers.ExtendableObjectParser;
import redactedrice.modularparser.reserved.DefaultReservedWordSupporter;
import redactedrice.modularparser.scope.DefaultScopeSupporter;
import redactedrice.modularparser.scope.DefaultScopedVarConstParser;
import redactedrice.modularparser.testsupport.SimpleObject;
import redactedrice.modularparser.testsupport.SimpleObjectLiteralParser;
import redactedrice.reflectionhelpers.objects.ExtendableObject;

public class ObjectCreationAndChainingTests {

    ModularParser parser;
    DefaultLineFormerSupporter reader;
    DefaultScopedVarConstParser varParser;

    @BeforeEach
    void setup() {
        parser = new ModularParser();
        reader = new DefaultLineFormerSupporter();
        parser.addModule(reader);
        parser.addModule(new DefaultLineParserSupporter());
        parser.addModule(new DefaultLiteralSupporter());
        parser.addModule(new DefaultReservedWordSupporter());
        parser.addModule(new DefaultConsoleLogHandler());

        DefaultScopeSupporter scope = new DefaultScopeSupporter("BasicScopeHandler", true);
        scope.pushScope("global");
        scope.pushScope("file");
        parser.addModule(scope);

        parser.addModule(
                new DefaultGroupingLineModifier("BasicParenthesisModule", "(", ")", false));

        parser.addModule(new DefaultNumberLiteralParser());
        parser.addModule(new DefaultCharLiteralParser());
        parser.addModule(new DefaultBoolLiteralParser());
        parser.addModule(new SimpleObjectLiteralParser());
        parser.addModule(new ExtendableObjectParser());
        varParser = new DefaultScopedVarConstParser("BasicVarHandler", true, "var");
        parser.addModule(varParser);
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
        parser.parse();

        assertTrue(varParser.isVariable("so"));
        SimpleObject so = (SimpleObject) varParser.getVariableValue("so").value();
        assertEquals(1, so.intField);
        assertTrue(so.boolField);
        assertEquals("so1", so.strField);

        assertTrue(varParser.isVariable("eo"));
        ExtendableObject eo = (ExtendableObject) varParser.getVariableValue("eo").value();
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
        parser.parse();

        assertTrue(varParser.isVariable("obj3"));
        SimpleObject obj3 = (SimpleObject) varParser.getVariableValue("obj3").value();
        assertEquals(obj3.intField, 3);
        assertEquals(obj3.so.intField, 2);
        assertEquals(obj3.so.so.intField, 1);

        assertTrue(varParser.isVariable("obj4"));
        SimpleObject obj4 = (SimpleObject) varParser.getVariableValue("obj4").value();
        assertEquals(obj4.intField, 4);
        assertEquals(obj4.so.intField, 5);
        assertEquals(obj4.so.so.intField, 6);

        assertTrue(varParser.isVariable("eo"));
        ExtendableObject eo = (ExtendableObject) varParser.getVariableValue("eo").value();
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
        parser.parse();

        assertTrue(varParser.isVariable("obj3"));
        SimpleObject obj3 = (SimpleObject) varParser.getVariableValue("obj3").value();
        assertEquals(3, obj3.intField);
        assertEquals(2, obj3.so.intField);
        assertEquals(5, obj3.so.so.intField);

        assertTrue(varParser.isVariable("intField"));
        int intField = (int) varParser.getVariableValue("intField").value();
        assertEquals(1, intField);

        assertTrue(varParser.isVariable("boolField"));
        boolean boolField = (boolean) varParser.getVariableValue("boolField").value();
        assertTrue(boolField);
    }
}
