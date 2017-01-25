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

import java.util.*;

/**
 * Helper class to create the xmlns: namespace prefixes.
 *
 * @author Matthias Ronge
 */
public class Namespace {
    /**
     * Maps the constant namespaces "xml:" and "xmlns:" to their reserved
     * prefixes. The map is populated in a static block below.
     */
    private static final Map<String, String> CONSTANTS = new HashMap<>(3);

    /**
     * The "xml:" prefix which is reserved and must neither be used otherwise
     * nor be named differently. In this class, all prefix Strings are without
     * the colon.
     */
    private static final String PREFIX_XML = "xml";

    /**
     * The "xmlns:" prefix which is reserved and must neither be used otherwise
     * nor be named differently. In this class, all prefix Strings are without
     * the colon.
     */
    private static final String PREFIX_XMLNS = "xmlns";
    /**
     * The XML namespace URL.
     */
    private static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    /**
     * The XMLNS namespace URL.
     */
    private static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";

    /**
     * Populates the map with the constant namespaces "xml:" and "xmlns:".
     */
    static {
        CONSTANTS.put(XMLNS_NAMESPACE, PREFIX_XMLNS);
        CONSTANTS.put(XML_NAMESPACE, PREFIX_XML);
    }

    /**
     * Recomposes an URL from a namespace and a local name, adding a number sign
     * in between if the URL does not end in a slash.
     * 
     * @param namespace
     *            namespace for URL
     * @param localName
     *            local name for URL
     * @return combined URL
     */
    public static String concat(String namespace, String localName) {
        boolean addNumberSign = !namespace.endsWith("/") && !namespace.endsWith("#");
        StringBuilder result = new StringBuilder(namespace.length() + (addNumberSign ? 1 : 0) + localName.length());
        result.append(namespace);
        if (addNumberSign) {
            result.append('#');
        }
        result.append(localName);
        return result.toString();
    }

    /**
     * Returns the local name part of an URL. If the URL contains an anchor
     * symbol the local part is considered the sequence after it, otherwise the
     * local name is considered the sequence after the last slash.
     *
     * @param url
     *            URL to return the local name from
     * @return the local name
     */
    public static String localNameOf(String url) {
        int numberSign = url.indexOf('#');
        if (numberSign > -1) {
            return url.substring(numberSign + 1);
        }
        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * Returns the namespace part of an URL. If the URL contains an anchor
     * symbol the namespace is considered the sequence before it, omitting the
     * anchor symbol, otherwise the namespace is considered the sequence up to
     * and including the last slash.
     *
     * @param url
     *            url to return the namespace from
     * @return the namespace
     */
    public static String namespaceOf(String url) {
        int numberSign = url.indexOf('#');
        if (numberSign > -1) {
            return url.substring(0, numberSign);
        }
        return url.substring(0, url.lastIndexOf('/') + 1);
    }
}
