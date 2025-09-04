package redactedrice.modularparser.reserved;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Module;

class ReservedWordSupporterTests {

    private class ReservedWordSupporterTester implements ReservedWordSupporter {
        @Override
        public void handleModule(Module module) { /* not needed for testing */}

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setParser(ModularParser parser) { /* not needed for testing */}

        @Override
        public void setModuleRefs() { /* not needed for testing */}

        @Override
        public boolean checkModulesCompatibility() {
            return false;
        }

        @Override
        public String getReservedWordOwner(String word) {
            return null;
        }

        @Override
        public Set<String> getReservedWords() {
            return null;
        }
    }

    @Test
    void validStartStopTokensTest() {
        ReservedWordSupporter testee = spy(new ReservedWordSupporterTester());
        when(testee.getReservedWordOwner(any())).thenReturn("SomeModule");
        assertTrue(testee.isReservedWord("AnyWord"));

        when(testee.getReservedWordOwner(any())).thenReturn(null);
        assertFalse(testee.isReservedWord("DifferentWord"));
    }
}
