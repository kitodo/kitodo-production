package org.kitodo.lugh;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kitodo.lugh.vocabulary.*;

/** Tests {@code org.kitodo.lugh.Storage}. */
public class StorageTest {

    /** Tests {@code createLangString(String, String)}. */
    @Test
    public void testCreateLangStringStringString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code createLiteral(String, IdentifiableNode)}. */
    @Test
    public void testCreateLiteralStringIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code createLiteral(String, String)}. */
    @Test
    public void testCreateLiteralStringString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code createLiteralType(String, String)}. */
    @Test
    public void testCreateLiteralTypeStringString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code createNamedNode(String)}. */
    @Test
    public void testCreateNamedNodeString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code createNodeReference(String)}. */
    @Test
    public void testCreateNodeReferenceString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code createResult(Model, boolean)}. */
    @Test
    public void testCreateResultModelBoolean() {
        fail("Not yet implemented.");
    }

    /** Tests {@code createNode()}. */
    @Test
    public void testNodeCanBeCreatedEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode();
        }
    }

    /** Tests {@code createNode(NodeReference)}. */
    @Test
    public void testNodeCanBeCreatedWithNodeReferenceAsType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(Mets.METS);

            assertEquals(Mets.METS.getIdentifier(), mets.getType());
        }
    }

    /** Tests {@code createNode(String)}. */
    @Test
    public void testNodeCanBeCreatedWithStringAsType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            final String MODS = Mods.MODS.getIdentifier();
            Node mods = storage.createNode(MODS);

            assertEquals(MODS, mods.getType());
        }
    }

    /** Tests {@code createNode(String)} with empty string. */
    @Test
    public void testNodeCreatedWithEmptyStringAsTypeIsEmpty() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                try {
                    storage.createNode("");
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }
    }

    /**
     * Tests {@code createNode(NodeReference)} with null node reference. The
     * created node must not contain a rdf:type relation.
     */
    @Test
    public void testNodeCreatedWithUnitializedNodeReferenceIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node a = storage.createNode((NodeReference) null);
            assertTrue(a.isEmpty());
        }
    }

    /**
     * Tests {@code createNode(String)} with null string. The created node must
     * not contain a rdf:type relation.
     */
    @Test
    public void testNodeCreatedWithUnitializedStringIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node a = storage.createNode((String) null);
            assertTrue(a.isEmpty());
        }
    }
}
