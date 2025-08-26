package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.lineformer.DefaultContinuerLineModifier;

public class DefaultChainingChainableLiteralParserTests {

    private static String NAME = "DefaultChainerArrow";
    private static String TOKEN = "->";
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
        assertEquals(Optional.empty(), testee.tryParseLiteral(null));
        assertEquals(Optional.empty(), testee.tryParseLiteral("  "));
        assertEquals(Optional.empty(), testee.tryParseLiteral("something without chainer"));

        final Object lhsObject = "StringObj";
        final Object rhsObject = "StringObjToo";
        when(literalSupporter.evaluateLiteral("line with")).thenReturn(lhsObject);
        when(literalSupporter.evaluateChainedLiteral(lhsObject, "a continuer"))
                .thenReturn(rhsObject);
        assertEquals(Optional.of(rhsObject), testee.tryParseLiteral("line with -> a continuer"));

        // Try with a queue instead of a stack
        testee = spy(new DefaultChainingChainableLiteralParser(NAME, "<-", true, parser));
        testee.setParser(parser);
        testee.setModuleRefs();
        when(literalSupporter.evaluateLiteral("a continuer")).thenReturn(lhsObject);
        when(literalSupporter.evaluateChainedLiteral(lhsObject, "line with")).thenReturn(rhsObject);
        assertEquals(Optional.of(rhsObject), testee.tryParseLiteral("line with <- a continuer"));
    }

    @Test
    void tryEvaluateChainedLiteralTest() {
        assertEquals(Optional.empty(), testee.tryEvaluateChainedLiteral(null, null));
        assertEquals(Optional.empty(), testee.tryEvaluateChainedLiteral(null, "  "));
        assertEquals(Optional.empty(),
                testee.tryEvaluateChainedLiteral(null, "something without chainer"));

        final Object startObject = "StringObjStarting";
        final Object lhsObject = "StringObj";
        final Object rhsObject = "StringObjToo";
        when(literalSupporter.evaluateChainedLiteral(startObject, "line with"))
                .thenReturn(lhsObject);
        when(literalSupporter.evaluateChainedLiteral(lhsObject, "a continuer"))
                .thenReturn(rhsObject);
        assertEquals(Optional.of(rhsObject),
                testee.tryEvaluateChainedLiteral(startObject, "line with -> a continuer"));
    }
}
