package redactedrice.modularparser.reserved;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Module;

public class ReservedWordSupporterTests {

    private class ReservedWordSupporterTester implements ReservedWordSupporter {
        @Override
        public void handleModule(Module module) {}

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setParser(ModularParser parser) {}

        @Override
        public void setModuleRefs() {}

        @Override
        public boolean checkModulesCompatibility() {
            return false;
        }

        @Override
        public boolean isReservedWord(String word, Optional<ReservedType> type) {
            return false;
        }

        @Override
        public Set<String> getReservedWords(ReservedType type) {
            return null;
        }

        @Override
        public Map<String, ReservedType> getAllReservedWords() {
            return null;
        }
    }

    @Test
    void validStartStopTokensTest() {
        ReservedWordSupporter testee = spy(new ReservedWordSupporterTester());
        when(testee.isReservedWord(any(), any())).thenReturn(true);
        assertTrue(testee.isReservedWord("AnyWord"));
    }
}
