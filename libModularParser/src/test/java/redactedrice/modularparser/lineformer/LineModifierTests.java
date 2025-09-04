package redactedrice.modularparser.lineformer;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LineModifierTests {

    static final String NAME = "GroupingLineModifierTest";
    static final String START_TOKEN = "(";
    static final String END_TOKEN = ")";

    @Test
    void validStartStopTokensTest() {
        boolean isComplete = true;
        assertTrue(LineModifier.validStartStopTokens("", START_TOKEN, END_TOKEN, isComplete)
                .isEmpty());
        assertTrue(LineModifier.validStartStopTokens("1 2 3", START_TOKEN, END_TOKEN, isComplete)
                .isEmpty());
        assertTrue(LineModifier.validStartStopTokens("1 (2) 3", START_TOKEN, END_TOKEN, isComplete)
                .isEmpty());
        assertTrue(LineModifier
                .validStartStopTokens("()(1 (2) )3", START_TOKEN, END_TOKEN, isComplete).isEmpty());

        assertFalse(LineModifier
                .validStartStopTokens("1 (2 (3)", START_TOKEN, END_TOKEN, isComplete).isEmpty());
        assertFalse(LineModifier.validStartStopTokens("1 (2 3", START_TOKEN, END_TOKEN, isComplete)
                .isEmpty());
        assertFalse(LineModifier
                .validStartStopTokens("1 )(2) (3", START_TOKEN, END_TOKEN, isComplete).isEmpty());

        isComplete = false;
        assertTrue(LineModifier.validStartStopTokens("", START_TOKEN, END_TOKEN, isComplete)
                .isEmpty());
        assertTrue(LineModifier.validStartStopTokens("1 2 3", START_TOKEN, END_TOKEN, isComplete)
                .isEmpty());
        assertTrue(LineModifier.validStartStopTokens("1 (2) 3", START_TOKEN, END_TOKEN, isComplete)
                .isEmpty());
        assertTrue(LineModifier
                .validStartStopTokens("()(1 (2) )3", START_TOKEN, END_TOKEN, isComplete).isEmpty());

        assertTrue(LineModifier.validStartStopTokens("1 (2 (3)", START_TOKEN, END_TOKEN, isComplete)
                .isEmpty());
        assertTrue(LineModifier.validStartStopTokens("1 (2 3", START_TOKEN, END_TOKEN, isComplete)
                .isEmpty());

        assertFalse(LineModifier
                .validStartStopTokens("1 )(2) (3", START_TOKEN, END_TOKEN, isComplete).isEmpty());

        // Ensure it handles partial tokens
        assertTrue(LineModifier.validStartStopTokens("(( ))(", "((", "))", isComplete).isEmpty());
    }
}
