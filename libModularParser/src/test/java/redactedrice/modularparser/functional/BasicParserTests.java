package redactedrice.modularparser.functional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.comment.DefaultMutliLineCommentLineModifier;
import redactedrice.modularparser.comment.DefaultSingleLineCommentLineModifier;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.lineformer.DefaultGroupingLineModifier;
import redactedrice.modularparser.lineformer.DefaultLineFormerSupporter;
import redactedrice.modularparser.lineparser.DefaultLineParserSupporter;
import redactedrice.modularparser.literal.DefaultBoolLiteralParser;
import redactedrice.modularparser.literal.DefaultCharLiteralParser;
import redactedrice.modularparser.literal.DefaultLiteralSupporter;
import redactedrice.modularparser.literal.DefaultNumberLiteralParser;
import redactedrice.modularparser.log.DefaultConsoleLogHandler;
import redactedrice.modularparser.log.DefaultLogSupporter;
import redactedrice.modularparser.reflectionutilsparsers.ExtendableObjectParser;
import redactedrice.modularparser.reserved.DefaultReservedWordSupporter;
import redactedrice.modularparser.scope.DefaultScopeSupporter;
import redactedrice.modularparser.scope.DefaultScopedVarConstParser;
import redactedrice.modularparser.testsupport.SimpleObjectLiteralParser;

class BasicParserTests {
    ModularParser parser;
    DefaultLineFormerSupporter reader;
    DefaultScopedVarConstParser varParser;
    DefaultScopedVarConstParser constParser;

    @BeforeEach
    void setup() {
        parser = new ModularParser();
        reader = new DefaultLineFormerSupporter();
        parser.addModule(reader);
        parser.addModule(new DefaultLineParserSupporter());
        parser.addModule(new DefaultLiteralSupporter());
        parser.addModule(new DefaultReservedWordSupporter());
        parser.addModule(new DefaultLogSupporter());
        parser.addModule(new DefaultConsoleLogHandler()); // TODO: Add testing log handler
        
        parser.addModule(new DefaultSingleLineCommentLineModifier("DoubleSlashComments", "//"));
        parser.addModule(new DefaultMutliLineCommentLineModifier("MutlilineSlashStarComments", "/*", "*/"));

        DefaultScopeSupporter scope = new DefaultScopeSupporter(true);
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
        varParser = new DefaultScopedVarConstParser("BasicVarHandler", true, "var");;
        parser.addModule(varParser);
        constParser = new DefaultScopedVarConstParser("BasicConstHandler", false, "const");
        parser.addModule(constParser);
    }

    @Test
    void basicVarManipulationTests() {
        parser.configureModules();

        String script = """
        		/* basic test cases for creating variables */
                var testInt = 5
                const testDouble = 8d
                var testLong = 42l
                const testString = "test"
                var testBool = t
                var testChar = '\t'
                // Some copying
                var testInt2 = testInt
                const testInt3 = testInt
                var testDouble2 = testDouble
                var testLong2 = 100 // types can change
                //reassignment
                testLong2 = 200l
                testInt = 0
                """;

        // Run parser
        reader.setReader(new BufferedReader(new StringReader(script)));
        parser.parse();

        assertEquals(Integer.class, varParser.getVariableValue("testInt").getValue().getClass());
        assertEquals(0, varParser.getVariableValue("testInt").convert(Integer.class).getValue());
        
        assertEquals(Double.class, constParser.getVariableValue("testDouble").getValue().getClass());
        assertEquals(8.0, constParser.getVariableValue("testDouble").convert(Double.class).getValue());

        assertEquals(Long.class, varParser.getVariableValue("testLong").getValue().getClass());
        assertEquals(42L, varParser.getVariableValue("testLong").convert(Long.class).getValue());
        
        assertEquals(String.class, constParser.getVariableValue("testString").getValue().getClass());
        assertEquals("test", constParser.getVariableValue("testString").convert(String.class).getValue());

        assertEquals(Boolean.class, varParser.getVariableValue("testBool").getValue().getClass());
        assertTrue(varParser.getVariableValue("testBool").convert(Boolean.class).getValue());

        assertEquals(Character.class, varParser.getVariableValue("testChar").getValue().getClass());
        assertEquals('\t', varParser.getVariableValue("testChar").convert(Character.class).getValue());
        
        assertEquals(Integer.class, varParser.getVariableValue("testInt2").getValue().getClass());
        assertEquals(5, varParser.getVariableValue("testInt2").convert(Integer.class).getValue());
        
        assertEquals(Integer.class, constParser.getVariableValue("testInt3").getValue().getClass());
        assertEquals(5, constParser.getVariableValue("testInt3").convert(Integer.class).getValue());
        
        assertEquals(Double.class, varParser.getVariableValue("testDouble2").getValue().getClass());
        assertEquals(8.0, varParser.getVariableValue("testDouble2").convert(Double.class).getValue());

        assertEquals(Long.class, varParser.getVariableValue("testLong2").getValue().getClass());
        assertEquals(200L, varParser.getVariableValue("testLong2").convert(Long.class).getValue());

    }
}