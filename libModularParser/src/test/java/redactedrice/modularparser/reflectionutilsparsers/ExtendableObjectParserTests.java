package redactedrice.modularparser.reflectionutilsparsers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.literal.LiteralSupporter;
import redactedrice.reflectionhelpers.objects.ExtendableObject;

class ExtendableObjectParserTest {
    private ModularParser parser;
    private LiteralSupporter literalSupporter;
    private ExtendableObjectParser testee;

    static final String NAME = ExtendableObjectParser.class.getSimpleName();
    static final String CHAINED_ARG = "object";

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        literalSupporter = mock(LiteralSupporter.class);
        when(parser.getSupporterOfType(LiteralSupporter.class)).thenReturn(literalSupporter);
        testee = spy(new ExtendableObjectParser());
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void constructorSetModuleRefsTest() {
        assertEquals(NAME, testee.getName());
        assertEquals("extendableobject", testee.getKeyword());
        assertEquals(CHAINED_ARG, testee.getChainedArg());
        assertEquals(1, testee.getRequiredArgs().length);
        assertEquals(CHAINED_ARG, testee.getRequiredArgs()[0]);
        assertEquals(0, testee.getOptionalArgs().length);
        assertEquals(0, testee.getOptionalDefaults().length);
        assertEquals(literalSupporter, testee.getLiteralSupporter());
    }

    @Test
    void tryEvaluateObjectTest() {
        String obj = "stringObj";
        Map<String, Object> args = Map.of("object", obj);
        Response<Object> result = testee.tryEvaluateObject(args);
        assertTrue(result.wasValueReturned());
        assertEquals(obj, ((ExtendableObject) result.value()).getObject());
    }
}