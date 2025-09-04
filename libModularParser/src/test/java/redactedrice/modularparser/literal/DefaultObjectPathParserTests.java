package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Response;
import redactedrice.reflectionhelpers.utils.ReflectionUtils;

class DefaultObjectPathParserTest {
    private ModularParser parser;
    private LiteralSupporter literalSupporter;
    private DefaultObjectPathParser testee;

    final String NAME = "DefaultObjectPathParser";
    final String CHAIN_STR = ".";
    final String ARG_SEPERATOR = ",";

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        literalSupporter = mock(LiteralSupporter.class);
        when(parser.getSupporterOfType(LiteralSupporter.class)).thenReturn(literalSupporter);
        testee = spy(new DefaultObjectPathParser(NAME, CHAIN_STR, ARG_SEPERATOR, parser));
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void constructorSetModuleRefsTest() {
        assertEquals(NAME, testee.getName());
        assertEquals(CHAIN_STR, testee.chainingCharacter);
        assertEquals(ARG_SEPERATOR, testee.argDelimiter);
        verify(parser).addModule(
                argThat(arg -> arg.getClass().equals(DefaultChainingChainableLiteralParser.class)));
        assertEquals(literalSupporter, testee.literalSupporter);
    }

    @Test
    void handleWithArgsTest() {
        assertTrue(testee.handleWithArgs(null, "anything").wasNotHandled());
        assertTrue(testee.handleWithArgs("anything", "non-matching").wasNotHandled());

        final String noArgs = "no arg";
        final String args = "3 args";
        final String argsString = "method(1, 2, 3)";

        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.notHandled());
        assertTrue(testee.handleWithArgs("anything", argsString).wasError());
        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.error("test"));
        assertTrue(testee.handleWithArgs("anything", argsString).wasError());

        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.is(1))
                .thenReturn(Response.is(2)).thenReturn(Response.is(3));
        try (MockedStatic<ReflectionUtils> mock = mockStatic(ReflectionUtils.class)) {
            mock.when(() -> ReflectionUtils.invoke(any(), any())).thenReturn(noArgs);
            mock.when(() -> ReflectionUtils.invoke(any(), any(), any(), any(), any()))
                    .thenReturn(args);

            assertEquals(Response.is(noArgs), testee.handleWithArgs("anything", "method()"));
            assertEquals(Response.is(args), testee.handleWithArgs("anything", "method(1, 2, 3)"));

            mock.when(() -> ReflectionUtils.invoke(any(), any()))
                    .thenThrow(new NoSuchMethodException());
            assertTrue(testee.handleWithArgs("anything", "method()").wasNotHandled());
        }
    }

    @Test
    void tryParseLiteralTest() {
        assertTrue(testee.tryParseLiteral("no period").wasNotHandled());

        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.is("anything"));
        assertTrue(testee.tryParseLiteral("has.period").wasHandled());

        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.notHandled());
        assertTrue(testee.tryParseLiteral("has.period").wasError());
    }

    @Test
    void tryEvaluateChainedLiteralTest() {
        assertTrue(testee.tryEvaluateChainedLiteral(null, "anything").wasNotHandled());
        assertTrue(testee.tryEvaluateChainedLiteral("anything", null).wasNotHandled());

        doReturn(Response.is("anything")).when(testee).handleWithArgs(any(), any());
        assertTrue(testee.tryEvaluateChainedLiteral("anything", "anythingToo").wasHandled());

        doReturn(Response.notHandled()).when(testee).handleWithArgs(any(), any());
        try (MockedStatic<ReflectionUtils> mock = mockStatic(ReflectionUtils.class)) {
            mock.when(() -> ReflectionUtils.getVariable(any(), any())).thenReturn("something");
            assertTrue(testee.tryEvaluateChainedLiteral("anything", "anythingToo").wasHandled());

            mock.when(() -> ReflectionUtils.getVariable(any(), any()))
                    .thenThrow(new NoSuchFieldException());
            assertTrue(testee.tryEvaluateChainedLiteral("anything", "anythingToo").wasNotHandled());
        }
    }
}