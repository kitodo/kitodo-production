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

/**
 * The {@code http://www.w3.org/2000/01/rdf-schema} namespace.
 */
public class RDFS {

    /**
     * The class of classes.
     */
    public static final NodeReference CLASS = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#Class");

    /**
     * A description of the subject resource.
     */
    public static final NodeReference COMMENT = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#comment");

    /**
     * The class of RDF containers.
     */
    public static final NodeReference CONTAINER = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#Container");

    /**
     * The class of container membership properties, rdf:_1, rdf:_2, ...,<br>
     * all of which are sub-properties of 'member'.
     */
    public static final NodeReference CONTAINER_MEMBERSHIP_PROPERTY = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty");

    /**
     * The class of RDF datatypes.
     */
    public static final NodeReference DATATYPE = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#Datatype");

    /**
     * A domain of the subject property.
     */
    public static final NodeReference DOMAIN = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#domain");

    /**
     * The defininition of the subject resource.
     */
    public static final NodeReference IS_DEFINED_BY = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#isDefinedBy");

    /**
     * A human-readable name for the subject.
     */
    public static final NodeReference LABEL = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#label");

    /**
     * The class of literal values, eg. textual strings and integers.
     */
    public static final NodeReference LITERAL = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#Literal");

    /**
     * A member of the subject resource.
     */
    public static final NodeReference MEMBER = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#member");

    /**
     * The RDFS namespace.
     */
    public static final String NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";
    /**
     * A range of the subject property.
     */
    public static final NodeReference RANGE = new MemoryNodeReference("http://www.w3.org/2000/01/rdf-schema#range");
    /**
     * The class resource, everything.
     */
    public static final NodeReference RESOURCE = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#Resource");

    /**
     * A reverse map of the URLs to the constants.
     */
    private static final Map<String, NodeReference> reversed;
    /**
     * Further information about the subject resource.
     */
    public static final NodeReference SEE_ALSO = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#seeAlso");

    /**
     * The subject is a subclass of a class.
     */
    public static final NodeReference SUB_CLASS_OF = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#subClassOf");

    /**
     * The subject is a subproperty of a property.
     */
    public static final NodeReference SUB_PROPERTY_OF = new MemoryNodeReference(
            "http://www.w3.org/2000/01/rdf-schema#subPropertyOf");

    /**
     * Populates the reverse map of the URLs to the constants.
     */
    static {
        reversed = new HashMap<>(20);
        for (NodeReference value : new NodeReference[] {DATATYPE, SUB_PROPERTY_OF, IS_DEFINED_BY, DOMAIN, SUB_CLASS_OF,
                MEMBER, LITERAL, CLASS, COMMENT, CONTAINER_MEMBERSHIP_PROPERTY, CONTAINER, RESOURCE, LABEL, SEE_ALSO,
                RANGE }) {
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
