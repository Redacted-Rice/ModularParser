package redactedrice.modularparser.reflectionhelpersparsers;


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
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.LiteralSupporter;
import redactedrice.modularparser.literal.argumented.BaseArgumentedChainableLiteral;
import redactedrice.reflectionhelpers.objects.ExtendableObject;

class ExtendableObjectParserTest {
    private ModularParser parser;
    private LiteralSupporter literalSupporter;
    private Grouper grouper;
    private ExtendableObjectParser testee;

    static final String NAME = ExtendableObjectParser.class.getSimpleName();
    static final String CHAINED_ARG = "object";

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        literalSupporter = mock(LiteralSupporter.class);
        grouper = mock(Grouper.class);

        when(parser.getSupporterOfType(LiteralSupporter.class)).thenReturn(literalSupporter);
        testee = spy(new ExtendableObjectParser(grouper));
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void defaultGrouperTest() {
        BaseArgumentedChainableLiteral.setDefaultGrouper(grouper);
        assertEquals(grouper, BaseArgumentedChainableLiteral.getDefaultGrouper());
        ExtendableObjectParser defaultGrouper = new ExtendableObjectParser();
        assertEquals(grouper, defaultGrouper.getGrouper());

        // Set it back to null for other tests and test that constructor ensures not null
        BaseArgumentedChainableLiteral.setDefaultGrouper(null);
    }

    @Test
    void constructorSetModuleRefsTest() {
        assertEquals(NAME, testee.getName());
        assertEquals("extendableobject", testee.getKeyword());
        assertEquals(CHAINED_ARG, testee.getChainedArg());
        assertEquals(1, testee.getArgsDef().getNumRequiredArgs());
        assertEquals(CHAINED_ARG, testee.getArgsDef().getRequiredArg(0));
        assertEquals(0, testee.getArgsDef().getNumOptionalArgs());
        assertEquals(literalSupporter, testee.getLiteralSupporter());
    }

    @Test
    void tryEvaluateObjectTest() {
        String obj = "stringObj";
        Map<String, Object> args = Map.of("object", obj);
        Response<Object> result = testee.tryEvaluateObject(args);
        assertTrue(result.wasValueReturned());
        assertEquals(obj, ((ExtendableObject) result.getValue()).getObject());
    }
}