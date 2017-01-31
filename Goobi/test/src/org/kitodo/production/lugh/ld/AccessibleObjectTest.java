/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General private License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.lugh.ld;

import static org.junit.Assert.*;

import java.nio.BufferOverflowException;
import java.util.NoSuchElementException;

import org.junit.Test;

public class AccessibleObjectTest {

    @Test
    public void testGetTypeForALiteral() {
        LangString langString = new LangString("Kitodo. Key to digital objects", "de");
        assertEquals(RDF.LANG_STRING.getIdentifier(), ((AccessibleObject) langString).getType());
    }

    @Test
    public void testGetTypeForANodeWithExactlyOneType() {
        final String SOME_CLASS = "http://names.kitodo.org/tests#SomeClass";
        assertEquals(SOME_CLASS, ((AccessibleObject) new Node(SOME_CLASS)).getType());
    }

    @Test(expected=NoSuchElementException.class)
    public void testGetTypeForATypelessNode() {
        ((AccessibleObject) new Node()).getType();
    }

    @Test(expected=BufferOverflowException.class)
    public void testGetTypeForANodeThatHasMultipleTypes() {
        Node node = new Node("http://names.kitodo.org/tests#SomeClass");
        node.put(RDF.TYPE, "http://names.kitodo.org/tests#SomeOtherClass");
        ((AccessibleObject) node).getType();
    }

    @Test
    public void testMatchesForANodeThatShouldMatch() {
        NamedNode kitodo = new NamedNode("http://kitodo.org/");
        kitodo.put(RDF.TYPE, "http://de.wikipedia.org/wiki/Verein#Eingetragener_Verein");
        kitodo.put(RDF.TYPE, "http://schema.org/Organization");
        kitodo.put("http://schema.org/name", new LangString("Kitodo. Key to digital objects", "de"));
        kitodo.put("http://xmlns.com/foaf/0.1/page", "https://www.kitodo.org/");

        assertTrue(((AccessibleObject) kitodo).matches(new Node("http://schema.org/Organization")));
        assertTrue(((AccessibleObject) kitodo).matches(new Node("http://de.wikipedia.org/wiki/Verein#Eingetragener_Verein").put("http://schema.org/name", new LangString("Kitodo. Key to digital objects", "de"))));
    }

    @Test
    public void testMatchesForANodeThatShouldNotMatch() {
        NamedNode kitodo = new NamedNode("http://kitodo.org/");
        kitodo.put(RDF.TYPE, "http://de.wikipedia.org/wiki/Verein#Eingetragener_Verein");
        kitodo.put(RDF.TYPE, "http://schema.org/Organization");
        kitodo.put("http://schema.org/name", new LangString("Kitodo. Key to digital objects", "de"));
        kitodo.put("http://xmlns.com/foaf/0.1/page", "https://www.kitodo.org/");

        assertFalse(((AccessibleObject) kitodo).matches(new Node("http://schema.org/SportsOrganization")));
    }
}
