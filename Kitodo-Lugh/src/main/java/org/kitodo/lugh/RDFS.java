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

package org.kitodo.lugh;

import java.util.*;

import org.kitodo.lugh.*;

/**
 * The {@code http://www.w3.org/2000/01/rdf-schema} namespace.
 */
public class RDFS {
    public static final NodeReference CLASS = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#Class");

    public static final NodeReference COMMENT = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#comment");

    public static final NodeReference CONTAINER_MEMBERSHIP_PROPERTY = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty");

    public static final NodeReference DATATYPE = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#Datatype");

    public static final NodeReference DOMAIN = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#domain");

    public static final NodeReference LABEL = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#label");

    public static final NodeReference MEMBER = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#member");

    public static final String NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

    public static final NodeReference RANGE = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#range");

    public static final NodeReference RESOURCE = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#Resource");

    /**
     * A reverse map of the URLs to the constants.
     */
    private static final Map<String, NodeReference> reversed;

    public static final NodeReference SUB_CLASS_OF = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#subClassOf");

    public static final NodeReference SUB_PROPERTY_OF = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#subPropertyOf");

    /**
     * Populates the reverse map of the URLs to the constants.
     */
    static {
        reversed = new HashMap<>(15);
        for (NodeReference value : new NodeReference[] {CLASS, COMMENT, CONTAINER_MEMBERSHIP_PROPERTY, DATATYPE, DOMAIN,
                LABEL, MEMBER, RANGE, RESOURCE, SUB_CLASS_OF, SUB_PROPERTY_OF }) {
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

    private RDFS() {
    }
}
