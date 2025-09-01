package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.DefaultContinuerLineModifier;

public class DefaultChainingChainableLiteralParserTests {

    private static String NAME = "DefaultChainerArrow";
    private static String TOKEN = "<-";
    private ModularParser parser;
    private LiteralSupporter literalSupporter;
    private DefaultChainingChainableLiteralParser testee;

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        literalSupporter = mock(LiteralSupporter.class);
        when(parser.getSupporterOfType(LiteralSupporter.class)).thenReturn(literalSupporter);
        testee = spy(new DefaultChainingChainableLiteralParser(NAME, TOKEN, false, parser));
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void constructorSetModuleRefsTest() {
        assertEquals(NAME, testee.getName());
        assertEquals(TOKEN, testee.chainingToken);
        // Verify the line continuer was added
        verify(parser).addModule(
                argThat(arg -> arg.getClass().equals(DefaultContinuerLineModifier.class)));
        assertEquals(literalSupporter, testee.literalSupporter);
    }

    @Test
    void tryParseLiteralTest() {
    	doReturn(Response.notHandled()).when(testee).tryEvaluateInternal(any(), eq(false), eq(null));
        assertTrue(testee.tryParseLiteral("anything").wasNotHandled());

    }

    @Test
    void tryEvaluateChainedLiteral() {
    	final Object chained = "Something";
    	doReturn(Response.notHandled()).when(testee).tryEvaluateInternal(any(), eq(true), eq(chained));
        assertTrue(testee.tryEvaluateChainedLiteral(chained, "anything").wasNotHandled());
    }

    @Test
    void tryEvaluateInternalTest() {
        assertEquals(Response.notHandled(), testee.tryParseLiteral("something without chainer"));

        final Object lhsObject = "StringObj";
        final Object rhsObject = "StringObjToo";
        final String lhs = "line with";
        final String rhs = "a continuer";
        final String line = "line with <- a continuer";

        // Bad first response
        when(literalSupporter.evaluateLiteral(rhs)).thenReturn(Response.notHandled());
        assertTrue(testee.tryEvaluateInternal(line, false, null).wasError());
        when(literalSupporter.evaluateLiteral(rhs)).thenReturn(Response.error("test"));
        assertTrue(testee.tryEvaluateInternal(line, false, null).wasError());
        
        when(literalSupporter.evaluateLiteral(rhs)).thenReturn(Response.is(rhsObject));
        when(literalSupporter.evaluateChainedLiteral(rhsObject, lhs))
                .thenReturn(Response.is(lhsObject));
        assertEquals(Response.is(lhsObject), testee.tryEvaluateInternal(line, false, null));

        // Bad second response
        when(literalSupporter.evaluateChainedLiteral(any(), eq(lhs))).thenReturn(Response.notHandled());
        assertTrue(testee.tryEvaluateInternal(line, false, null).wasError());
        when(literalSupporter.evaluateChainedLiteral(any(), eq(lhs))).thenReturn(Response.error("test"));
        assertTrue(testee.tryEvaluateInternal(line, false, null).wasError());
        
        // Try with a queue instead of a stack
        testee = spy(new DefaultChainingChainableLiteralParser(NAME, "->", true, parser));
        testee.setParser(parser);
        testee.setModuleRefs();
        when(literalSupporter.evaluateChainedLiteral(any(), eq(lhs))).thenReturn(Response.is(lhsObject));
        when(literalSupporter.evaluateChainedLiteral(lhsObject, rhs)).thenReturn(Response.is(rhsObject));
        assertEquals(Response.is(rhsObject), testee.tryEvaluateInternal("line with -> a continuer", true, "anything"));
    }
}
