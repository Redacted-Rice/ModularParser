package redactedrice.modularparser.scope;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.Response;

public class ScopeSupporterTests {

    private static String NAME = "ScopeSupporter";

    private class ScopeSupporterTester implements ScopeSupporter {
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
        public boolean pushScope(String scope) {
            return false;
        }

        @Override
        public boolean popScope() {
            return false;
        }

        @Override
        public boolean removeScope(String scope) {
            return false;
        }

        @Override
        public String currentScope() {
            return null;
        }

        @Override
        public String[] splitScope(String logicalLine) {
            return null;
        }

        @Override
        public String getOwner(String scope, String name) {
            return null;
        }

        @Override
        public String getNarrowestScope(String name) {
            return null;
        }

        @Override
        public Response<Object> getData(String scope, String name, Module owner) {
            return null;
        }

        @Override
        public Set<String> getAllOwnedNames(String scope, Module owner) {
            return null;
        }

        @Override
        public Map<String, Object> getAllOwnedData(String scope, Module owner) {
            return null;
        }

        @Override
        public boolean setData(String scope, String name, Module owner, Object data) {
            return false;
        }
    }

    @Test
    void validStartStopTokensTest() {
        ScopeSupporter testee = spy(new ScopeSupporterTester());
        when(testee.getName()).thenReturn(NAME);

        when(testee.getOwner(any(), any())).thenReturn(NAME);
        assertTrue(testee.doesOwn(testee, "global", "foo"));

        when(testee.getOwner(any(), any())).thenReturn("NotMyName");
        assertFalse(testee.doesOwn(testee, "global", "foo"));
    }
}
