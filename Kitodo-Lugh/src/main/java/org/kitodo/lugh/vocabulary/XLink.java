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
 * The {@code http://www.w3.org/1999/xlink} namespace.
 *
 * @author Matthias Ronge
 */
public class XLink {

    public static final NodeReference FROM = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/1999/xlink#from");

    public static final NodeReference HREF = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/1999/xlink#href");

    public static final String NAMESPACE = "http://www.w3.org/1999/xlink#";

    /**
     * A reverse map of the URLs to the constants.
     */
    private static final Map<String, NodeReference> reversed;

    public static final NodeReference TO = MemoryStorage.INSTANCE.createNodeReference("http://www.w3.org/1999/xlink#to");

    /**
     * Populates the reverse map of the URLs to the constants.
     */
    static {
        reversed = new HashMap<>(4);
        for (NodeReference value : new NodeReference[] {FROM, HREF, TO }) {
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

    private XLink() {
    }

}
