package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.Response;

public class DefaultLiteralSupporterTests {

    private static String MOD1_NAME = "LiteralParser1";
    private static String MOD2_NAME = "LiteralParser2";
    private static String MOD3_NAME = "ChainableLiteralParser1";
    private static String MOD4_NAME = "ChainableLiteralParser2";

    private DefaultLiteralSupporter testee;
    LiteralParser mod1;
    LiteralParser mod2;
    ChainableLiteralParser mod3;
    ChainableLiteralParser mod4;

    @BeforeEach
    void setup() {
        mod1 = mock(LiteralParser.class);
        mod2 = mock(LiteralParser.class);
        mod3 = mock(ChainableLiteralParser.class);
        mod4 = mock(ChainableLiteralParser.class);

        when(mod1.getName()).thenReturn(MOD1_NAME);
        when(mod2.getName()).thenReturn(MOD2_NAME);
        when(mod3.getName()).thenReturn(MOD3_NAME);
        when(mod4.getName()).thenReturn(MOD4_NAME);
    }

    @Test
    void constructorTest() {
        testee = new DefaultLiteralSupporter();
        assertEquals("DefaultLiteralSupportModule", testee.getName());
    }

    @Test
    void handleModuleTest() {
        testee = new DefaultLiteralSupporter();

        Module modOther = mock(Module.class);

        testee.handleModule(mod3);
        testee.handleModule(mod1);
        testee.handleModule(mod2);
        testee.handleModule(mod4);
        testee.handleModule(modOther);

        assertEquals(4, testee.handlers.size());
        assertTrue(testee.handlers.contains(mod1));
        assertTrue(testee.handlers.contains(mod2));
        assertTrue(testee.handlers.contains(mod3));
        assertTrue(testee.handlers.contains(mod4));

        assertEquals(2, testee.chainedHandlers.size());
        assertTrue(testee.chainedHandlers.contains(mod3));
        assertTrue(testee.chainedHandlers.contains(mod4));
    }

    @Test
    void evaluateLiteralTest() {
        testee = new DefaultLiteralSupporter();
        testee.handleModule(mod1);
        testee.handleModule(mod2);
        testee.handleModule(mod3);
        testee.handleModule(mod4);
        
        assertTrue(testee.evaluateLiteral(null).wasError());
        assertTrue(testee.evaluateLiteral("   ").wasError());

        when(mod1.tryParseLiteral(any())).thenReturn(Response.notHandled());
        when(mod2.tryParseLiteral(any())).thenReturn(Response.notHandled());
        when(mod3.tryParseLiteral(any())).thenReturn(Response.notHandled());
        when(mod4.tryParseLiteral(any())).thenReturn(Response.notHandled());

        assertEquals(Response.notHandled(), testee.evaluateLiteral("any string"));
        verify(mod1).tryParseLiteral(any());
        verify(mod2).tryParseLiteral(any());
        verify(mod3).tryParseLiteral(any());
        verify(mod4).tryParseLiteral(any());

        when(mod3.tryParseLiteral(any())).thenReturn(Response.is(42));
        assertEquals(Response.is(42), testee.evaluateLiteral("42"));
        verify(mod1, times(2)).tryParseLiteral(any());
        verify(mod2, times(2)).tryParseLiteral(any());
        verify(mod3, times(2)).tryParseLiteral(any());
        verify(mod4).tryParseLiteral(any());
    }

    @Test
    void evaluateChainedLiteralTest() {
        testee = new DefaultLiteralSupporter();
        testee.handleModule(mod1);
        testee.handleModule(mod2);
        testee.handleModule(mod3);
        testee.handleModule(mod4);
        
        assertTrue(testee.evaluateChainedLiteral("anything", null).wasError());
        assertTrue(testee.evaluateChainedLiteral("anything", "   ").wasError());

        when(mod3.tryEvaluateChainedLiteral(any(), any())).thenReturn(Response.notHandled());
        when(mod4.tryEvaluateChainedLiteral(any(), any())).thenReturn(Response.notHandled());

        assertEquals(Response.notHandled(), testee.evaluateChainedLiteral("object", "any string"));
        verify(mod3).tryEvaluateChainedLiteral(any(), any());
        verify(mod4).tryEvaluateChainedLiteral(any(), any());

        when(mod3.tryEvaluateChainedLiteral(any(), any())).thenReturn(Response.is(42));
        assertEquals(Response.is(42), testee.evaluateChainedLiteral("object", "42"));
        verify(mod3, times(2)).tryEvaluateChainedLiteral(any(), any());
        verify(mod4).tryEvaluateChainedLiteral(any(), any());
    }
}
