/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.dataaccess.storage.memory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.kitodo.dataaccess.RDF;

public class MemoryStorageTest {

    private static final MemoryNodeReference METS_METS = new MemoryNodeReference("http://www.loc.gov/METS/mets");

    /**
     * Tests {@code createLangString(String, String)}.
     */
    @Test
    public void testCreateLangString() {
        assertThat(
            MemoryStorage.INSTANCE
                    .createLangString("Die Zukunft wird nicht gemeistert von denen, die am Vergangenen kleben.", "de"),
            is(equalTo(new MemoryLangString("Die Zukunft wird nicht gemeistert von denen, die am Vergangenen kleben.",
                    "de"))));

    }

    /**
     * Tests {@code createLeaf(String, String)}.
     */
    @Test
    public void testCreateLeafWithHttpsUrl() {
        assertThat(MemoryStorage.INSTANCE.createLeaf("https://www.kitodo.org/", "…"),
            is(equalTo(new MemoryNodeReference("https://www.kitodo.org/"))));
    }

    /**
     * Tests {@code createLeaf(String, String)}.
     */
    @Test
    public void testCreateLeafWithHttpUrl() {
        assertThat(MemoryStorage.INSTANCE.createLeaf("http://www.kitodo.org/", "…"),
            is(equalTo(new MemoryNodeReference("http://www.kitodo.org/"))));
    }

    /**
     * Tests {@code createLeaf(String, String)}.
     */
    @Test
    public void testCreateLeafWithStringAndEmptyLanguage() {
        assertThat(MemoryStorage.INSTANCE.createLeaf("int i = 42;", ""),
            is(equalTo(new MemoryLiteral("int i = 42;", RDF.PLAIN_LITERAL))));
    }

    /**
     * Tests {@code createLeaf(String, String)}.
     */
    @Test
    public void testCreateLeafWithStringAndLanguage() {
        assertThat(MemoryStorage.INSTANCE.createLeaf("Unser Glück ist unmöglich ohne das Glück der anderen.", "de"),
            is(equalTo(new MemoryLangString("Unser Glück ist unmöglich ohne das Glück der anderen.", "de"))));
    }

    /**
     * Tests {@code createLeaf(String, String)}.
     */
    @Test
    public void testCreateLeafWithStringAndNullLanguage() {
        assertThat(MemoryStorage.INSTANCE.createLeaf("int i = 42;", null),
            is(equalTo(new MemoryLiteral("int i = 42;", RDF.PLAIN_LITERAL))));
    }

    /**
     * Tests {@code createLeaf(String, String)}.
     */
    @Test
    public void testCreateLeafWithUrn() {
        assertThat(MemoryStorage.INSTANCE.createLeaf("urn:nbn:de:1234567-789987876X", ""),
            is(equalTo(new MemoryNodeReference("urn:nbn:de:1234567-789987876X"))));
    }

    /**
     * Tests {@code createLiteral(String, IdentifiableNode)}.
     */
    @Test
    public void testCreateLiteralStringIdentifiableNode() {
        assertThat(MemoryStorage.INSTANCE.createLiteral("\\.ba\\, |delete operation|, 0xc0000034", RDF.PLAIN_LITERAL),
            is(equalTo(new MemoryLiteral("\\.ba\\, |delete operation|, 0xc0000034", RDF.PLAIN_LITERAL))));
    }

    /**
     * Tests {@code createLiteral(String, String)}.
     */
    @Test
    public void testCreateLiteralStringString() {
        assertThat(
            MemoryStorage.INSTANCE.createLiteral("\\.ba\\, |delete operation|, 0xc0000034",
                RDF.PLAIN_LITERAL.getIdentifier()),
            is(equalTo(
                new MemoryLiteral("\\.ba\\, |delete operation|, 0xc0000034", RDF.PLAIN_LITERAL.getIdentifier()))));
    }

    /**
     * Tests {@code createNamedNode(String)}.
     */
    @Test
    public void testCreateNamedNode() {
        assertThat(MemoryStorage.INSTANCE.createNamedNode("http://localhost/test/node1"),
            is(equalTo(new MemoryNamedNode("http://localhost/test/node1"))));

    }

    /**
     * Tests {@code createNode()}.
     */
    @Test
    public void testCreateNode() {
        assertThat(MemoryStorage.INSTANCE.createNode(), is(equalTo(new MemoryNode())));
    }

    /**
     * Tests {@code createNode(NodeReference)}.
     */
    @Test
    public void testCreateNodeNodeReference() {
        assertThat(MemoryStorage.INSTANCE.createNode(METS_METS), is(equalTo(new MemoryNode(METS_METS))));
    }

    /**
     * Tests {@code createNodeReference(String)}.
     */
    @Test
    public void testCreateNodeReference() {
        assertThat(MemoryStorage.INSTANCE.createNodeReference("http://localhost/test/node1"),
            is(equalTo(new MemoryNodeReference("http://localhost/test/node1"))));
    }

    /**
     * Tests {@code createNode(String)}.
     */
    @Test
    public void testCreateNodeString() {
        assertThat(MemoryStorage.INSTANCE.createNode("http://localhost/test/node1"),
            is(equalTo(new MemoryNode("http://localhost/test/node1"))));
    }

}
