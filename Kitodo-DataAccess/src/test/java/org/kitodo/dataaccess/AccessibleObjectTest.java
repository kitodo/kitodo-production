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

package org.kitodo.dataaccess;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.BufferOverflowException;
import java.util.NoSuchElementException;
import org.junit.Test;
import org.kitodo.dataaccess.AccessibleObject;
import org.kitodo.dataaccess.LangString;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Storage;

public class AccessibleObjectTest {

    @Test
    public void testGetTypeForALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString langString = storage.createLangString("Kitodo. Key to digital objects", "de");
            assertThat(((AccessibleObject) langString).getType(), is(equalTo(RDF.LANG_STRING.getIdentifier())));
        }
    }

    @Test
    public void testGetTypeForANodeThatHasMultipleTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode("http://names.kitodo.org/tests#SomeClass");
            node.put(RDF.TYPE, "http://names.kitodo.org/tests#SomeOtherClass");
            try {
                ((AccessibleObject) node).getType();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testGetTypeForANodeWithExactlyOneType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            final String SOME_CLASS = "http://names.kitodo.org/tests#SomeClass";
            assertThat(((AccessibleObject) storage.createNode(SOME_CLASS)).getType(), is(equalTo(SOME_CLASS)));
        }
    }

    @Test
    public void testGetTypeForATypelessNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                ((AccessibleObject) storage.createNode()).getType();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testMatchesForANodeThatShouldMatch() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node kitodo = storage.createNamedNode("http://kitodo.org/");
            kitodo.put(RDF.TYPE, "http://de.wikipedia.org/wiki/Verein#Eingetragener_Verein");
            kitodo.put(RDF.TYPE, "http://schema.org/Organization");
            kitodo.put("http://schema.org/name", storage.createLangString("Kitodo. Key to digital objects", "de"));
            kitodo.put("http://xmlns.com/foaf/0.1/page", "https://www.kitodo.org/");

            assertThat(((AccessibleObject) kitodo).matches(storage.createNode("http://schema.org/Organization")),
                is(true));
            assertTrue(((AccessibleObject) kitodo)
                    .matches(storage.createNode("http://de.wikipedia.org/wiki/Verein#Eingetragener_Verein").put(
                        "http://schema.org/name", storage.createLangString("Kitodo. Key to digital objects", "de"))));
        }
    }

    @Test
    public void testMatchesForANodeThatShouldNotMatch() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node kitodo = storage.createNamedNode("http://kitodo.org/");
            kitodo.put(RDF.TYPE, "http://de.wikipedia.org/wiki/Verein#Eingetragener_Verein");
            kitodo.put(RDF.TYPE, "http://schema.org/Organization");
            kitodo.put("http://schema.org/name", storage.createLangString("Kitodo. Key to digital objects", "de"));
            kitodo.put("http://xmlns.com/foaf/0.1/page", "https://www.kitodo.org/");

            assertFalse(
                ((AccessibleObject) kitodo).matches(storage.createNode("http://schema.org/SportsOrganization")));
        }
    }
}
