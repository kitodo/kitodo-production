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

package org.kitodo.lugh.vocabulary;

import java.util.*;

import org.kitodo.lugh.*;

/**
 * The {@code http://www.w3.org/XML/1998/namespace} namespace.
 *
 * @see "http://www.w3.org/XML/1998/namespace"
 */
public class XML {
    /**
     * A facility for defining base URIs for processing relative URI
     * refeferences is XML documents.
     *
     * @see "http://www.w3.org/TR/xmlbase/"
     */
    public static final NodeReference BASE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/XML/1998/namespace#base");

    /**
     * An attribute known to be of type ID that can be used independently of any
     * DTD or schema.
     *
     * @see "http://www.w3.org/TR/xml-id/"
     */
    public static final NodeReference ID = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/XML/1998/namespace#id");

    /**
     * Identifies the human language of the subject as a RFC 4646 code.
     */
    public static final NodeReference LANG = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/XML/1998/namespace#lang");
    /**
     * The namespace for "xml:".
     */
    public static final String NAMESPACE = "http://www.w3.org/XML/1998/namespace#";
    /**
     * A reverse map of the URLs to the constants.
     */
    private static final Map<String, NodeReference> reversed;
    /**
     * Expresses whether or not the wishes white space is to be considered as
     * significant in the scope of the subject.
     */
    public static final NodeReference SPACE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/XML/1998/namespace#space");

    /**
     * Populates the reverse map of the URLs to the constants.
     */
    static {
        reversed = new HashMap<>(6);
        for (NodeReference value : new NodeReference[] {BASE, ID, LANG, SPACE }) {
            reversed.put(value.getIdentifier(), value);
        }
    }

    /**
     * Returns a constant by its URL value
     *
     * @param url
     *            URL to resolve
     * @return the enum constant
     * @throws IllegalArgumentException
     *             if the URL is not mappped in the RDF namespace
     */
    public static NodeReference valueOf(String url) {
        if (!reversed.containsKey(url)) {
            throw new IllegalArgumentException("Unknown URL: " + url);
        }
        return reversed.get(url);
    }

    /**
     * Private constructor: You cannot create instances of this class.
     */
    private XML() {
    }
}
