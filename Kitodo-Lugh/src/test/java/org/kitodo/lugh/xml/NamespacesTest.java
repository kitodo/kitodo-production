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

package org.kitodo.lugh.xml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NamespacesTest {

    @Test
    public void testConcatWithHash() {
        assertEquals("http://names.kitodo.org/jUnitTest#identifier",
                Namespaces.concat("http://names.kitodo.org/jUnitTest#", "identifier"));
    }

    @Test
    public void testConcatWithNothing() {
        assertEquals("http://names.kitodo.org/jUnitTest#identifier",
                Namespaces.concat("http://names.kitodo.org/jUnitTest", "identifier"));
    }

    @Test
    public void testConcatWithSlash() {
        assertEquals("http://names.kitodo.org/jUnitTest/identifier",
                Namespaces.concat("http://names.kitodo.org/jUnitTest/", "identifier"));
    }

    @Test
    public void testLocalNameOfWithHash() {
        assertEquals("foo/bar", Namespaces.localNameOf("http://names.kitodo.org/jUnitTest#foo/bar"));
    }

    @Test
    public void testLocalNameOfWithSlash() {
        assertEquals("identifier", Namespaces.localNameOf("http://names.kitodo.org/jUnitTest/identifier"));
    }

    @Test
    public void testNamespaceOfWithHash() {
        assertEquals("http://names.kitodo.org/jUnitTest#",
                Namespaces.namespaceOf("http://names.kitodo.org/jUnitTest#foo/bar"));
    }

    @Test
    public void testNamespaceOfWithSlash() {
        assertEquals("http://names.kitodo.org/jUnitTest/",
                Namespaces.namespaceOf("http://names.kitodo.org/jUnitTest/identifier"));
    }
}
