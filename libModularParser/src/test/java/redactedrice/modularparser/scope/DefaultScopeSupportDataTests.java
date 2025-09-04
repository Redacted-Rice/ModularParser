package redactedrice.modularparser.scope;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Response;

public class DefaultScopeSupportDataTests {

    private static String MOD1_NAME = "ScopedParser1";
    private static String MOD2_NAME = "ScopedParser2";
    private static String SCOPE1 = "global";
    private static String SCOPE2 = "file";
    private static String VAR1 = "x";
    private static String VAR2 = "y";
    private static String UNUSED_VAR = "unused";
    private static OwnedObject OBJ1 = new OwnedObject(MOD1_NAME, 1);
    private static OwnedObject OBJ2 = new OwnedObject(MOD2_NAME, 2);
    private static OwnedObject OBJ3 = new OwnedObject(MOD2_NAME, 3);

    private ModularParser parser;
    private DefaultScopeSupporter testee;
    ScopedParser mod1;
    ScopedParser mod2;

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        mod1 = mock(ScopedParser.class);
        mod2 = mock(ScopedParser.class);

        when(mod1.getName()).thenReturn(MOD1_NAME);
        when(mod2.getName()).thenReturn(MOD2_NAME);

        testee = spy(new DefaultScopeSupporter(true));
        testee.setParser(parser);
        testee.pushScope(SCOPE1);
        testee.pushScope(SCOPE2);
        testee.handleModule(mod1);
        testee.handleModule(mod2);

        testee.scopedVals.get(SCOPE1).put(VAR1, OBJ1);
        testee.ownerMap.get(MOD1_NAME).get(SCOPE1).add(VAR1);

        testee.scopedVals.get(SCOPE2).put(VAR1, OBJ2);
        testee.ownerMap.get(MOD2_NAME).get(SCOPE2).add(VAR1);

        testee.scopedVals.get(SCOPE1).put(VAR2, OBJ3);
        testee.ownerMap.get(MOD2_NAME).get(SCOPE1).add(VAR2);
    }

    @Test
    void getDataForScopeOrNarrowestScopeTest() {
        // test with specified scope
        assertEquals(OBJ1, testee.getDataForScopeOrNarrowestScope(SCOPE1, VAR1));
        assertEquals(OBJ2, testee.getDataForScopeOrNarrowestScope(SCOPE2, VAR1));
        assertEquals(OBJ3, testee.getDataForScopeOrNarrowestScope(SCOPE1, VAR2));

        assertNull(testee.getDataForScopeOrNarrowestScope("Unused", VAR1));
        assertNull(testee.getDataForScopeOrNarrowestScope(SCOPE1, UNUSED_VAR));

        // Now using default scope
        assertEquals(OBJ2, testee.getDataForScopeOrNarrowestScope(null, VAR1));
        assertEquals(OBJ3, testee.getDataForScopeOrNarrowestScope(null, VAR2));
        assertNull(testee.getDataForScopeOrNarrowestScope(null, UNUSED_VAR));
        // Empty scope (also default)
        assertEquals(OBJ2, testee.getDataForScopeOrNarrowestScope("", VAR1));
    }

    @Test
    void getOwnerTest() {
        doReturn(null).when(testee).getDataForScopeOrNarrowestScope(any(), any());
        assertFalse(testee.getOwner(SCOPE1, VAR1).wasValueReturned());

        doReturn(OBJ1).when(testee).getDataForScopeOrNarrowestScope(any(), any());
        Response<String> response = testee.getOwner(SCOPE1, VAR1);
        assertTrue(response.wasValueReturned());
        assertEquals(MOD1_NAME, response.value());
    }

    @Test
    void getNarrowestScopeTest() {
        Response<String> response = testee.getNarrowestScope(VAR1);
        assertTrue(response.wasValueReturned());
        assertEquals(SCOPE2, response.value());

        response = testee.getNarrowestScope(VAR2);
        assertTrue(response.wasValueReturned());
        assertEquals(SCOPE1, response.value());

        assertFalse(testee.getNarrowestScope(UNUSED_VAR).wasValueReturned());
    }

    @Test
    void getDataTest() {
        doReturn(null).when(testee).getDataForScopeOrNarrowestScope(any(), any());
        assertEquals(Response.notHandled(), testee.getData(SCOPE1, VAR1, mod1));

        doReturn(OBJ3).when(testee).getDataForScopeOrNarrowestScope(any(), any());
        assertEquals(Response.notHandled(), testee.getData(SCOPE1, VAR2, mod1));
        assertEquals(Response.is(OBJ3.obj()), testee.getData(SCOPE1, VAR2, mod2));
    }

    @Test
    void getAllOwnedNamesTest() {
        Set<String> results = testee.getAllOwnedNames(SCOPE1, mod1);
        assertEquals(1, results.size());
        assertTrue(results.contains(VAR1));

        results = testee.getAllOwnedNames(SCOPE2, mod1);
        assertEquals(0, results.size());

        results = testee.getAllOwnedNames(null, mod1);
        assertEquals(1, results.size());
        assertTrue(results.contains(VAR1));
        results = testee.getAllOwnedNames("", mod1);
        assertEquals(1, results.size());
        assertTrue(results.contains(VAR1));

        results = testee.getAllOwnedNames(SCOPE1, mod2);
        assertEquals(1, results.size());
        assertTrue(results.contains(VAR2));

        results = testee.getAllOwnedNames(SCOPE2, mod2);
        assertEquals(1, results.size());
        assertTrue(results.contains(VAR1));

        results = testee.getAllOwnedNames(null, mod2);
        assertEquals(2, results.size());
        assertTrue(results.contains(VAR1));
        assertTrue(results.contains(VAR2));
        results = testee.getAllOwnedNames("", mod2);
        assertEquals(2, results.size());
        assertTrue(results.contains(VAR1));
        assertTrue(results.contains(VAR2));

        // Non existent owner/owns nothing
        ScopedParser mod3 = mock(ScopedParser.class);
        when(mod3.getName()).thenReturn("Module 3");
        results = testee.getAllOwnedNames(SCOPE2, mod3);
        assertEquals(0, results.size());

        testee.handleModule(mod3);
        results = testee.getAllOwnedNames(SCOPE2, mod3);
        assertEquals(0, results.size());
    }

    @Test
    void getAllOwnedDataTest() {
        Set<String> names = new HashSet<>();
        names.add(VAR1);
        doReturn(names).when(testee).getAllOwnedNames(any(), any());
        doReturn(OBJ1).when(testee).getDataForScopeOrNarrowestScope(any(), any());

        Map<String, Object> results = testee.getAllOwnedData(null, mod1);
        assertEquals(1, results.size());
        assertTrue(results.containsKey(VAR1));
        assertEquals(OBJ1.obj(), results.get(VAR1));
    }

    @Test
    void setDataTest() {
        final String VAR3 = "z";
        assertTrue(testee.setData(SCOPE1, VAR3, mod1, 5));
        assertTrue(testee.scopedVals.get(SCOPE1).containsKey(VAR3));
        assertEquals(MOD1_NAME, testee.scopedVals.get(SCOPE1).get(VAR3).owner());
        assertEquals(5, testee.scopedVals.get(SCOPE1).get(VAR3).obj());

        // Replace a val
        assertTrue(testee.setData(SCOPE1, VAR3, mod1, 42));
        assertTrue(testee.scopedVals.get(SCOPE1).containsKey(VAR3));
        assertEquals(MOD1_NAME, testee.scopedVals.get(SCOPE1).get(VAR3).owner());
        assertEquals(42, testee.scopedVals.get(SCOPE1).get(VAR3).obj());

        // Bad scope
        assertFalse(testee.setData("unused", VAR1, mod1, 42));

        // Bad Replace - different owner
        assertFalse(testee.setData(SCOPE1, VAR2, mod1, 42));
    }
}
