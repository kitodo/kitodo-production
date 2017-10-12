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

import java.math.BigInteger;
import java.util.*;

import org.kitodo.lugh.*;

/**
 * The {@code http://www.w3.org/1999/02/22-rdf-syntax-ns} namespace.
 */
public class RDF {
    /**
     * The RDF subject.
     */
    public static final NodeReference ABOUT = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#about");

    /**
     * The class of containers of alternatives.
     */
    public static final NodeReference ALT = new MemoryNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt");

    /**
     * The class of unordered containers.
     */
    public static final NodeReference BAG = new MemoryNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag");

    public static final NodeReference CLASS = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#Class");

    public static final NodeReference DESCRIPTION = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#Description");

    /**
     * The first item in the subject RDF list.
     */
    public static final NodeReference FIRST = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#first");

    /**
     * The datatype of RDF literals storing fragments of HTML content
     */
    public static final NodeReference HTML = new MemoryNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML");

    /**
     * The datatype of language-tagged string values
     */
    public static final NodeReference LANG_STRING = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString");

    /**
     * The class of RDF Lists.
     */
    public static final NodeReference LIST = new MemoryNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");

    /**
     * The RDF namespace.
     */
    public static final String NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /**
     * The empty list, with no items in it. If the rest of a list is nil then
     * the list has no more items in it.
     */
    public static final NodeReference NIL = new MemoryNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");

    /**
     * The object of the subject RDF statement.
     */
    public static final NodeReference OBJECT = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#object");

    /**
     * The class of plain (i.e. untyped) literal values, as used in RIF and OWL
     * 2
     */
    public static final NodeReference PLAIN_LITERAL = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral");

    /**
     * The predicate of the subject RDF statement.
     */
    public static final NodeReference PREDICATE = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");

    /**
     * The class of RDF properties.
     */
    public static final NodeReference PROPERTY = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");
    /**
     * The rest of the subject RDF list after the first item.
     */
    public static final NodeReference REST = new MemoryNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");

    /**
     * A reverse map of the URLs to the constants.
     */
    private static final Map<String, NodeReference> reversed;

    /**
     * The class of ordered containers.
     */
    public static final NodeReference SEQ = new MemoryNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq");

    /**
     * The prefix, followed by a natural integer, to indicate an element’s
     * position in an ordered data structure.
     *
     * @see "http://www.w3.org/TR/rdf-schema/#ch_containermembershipproperty"
     */
    public static final String SEQ_NO_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#_";

    /**
     * The length of the prefix which is indicating an element’s position in an
     * ordered data structure.
     */
    private static final int SEQ_NO_PREFIX_LENGTH = SEQ_NO_PREFIX.length();

    /**
     * The class of RDF statements.
     */
    public static final NodeReference STATEMENT = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement");

    /**
     * The subject of the subject RDF statement.
     */
    public static final NodeReference SUBJECT = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");

    /**
     * The subject is an instance of a class.
     */
    public static final NodeReference TYPE = new MemoryNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

    /**
     * Idiomatic property used for structured values.
     */
    public static final NodeReference VALUE = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#value");

    /**
     * The datatype of XML literal values.
     */
    public static final NodeReference XML_LITERAL = new MemoryNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");

    /**
     * Populates the reverse map of the URLs to the constants.
     */
    static {
        reversed = new HashMap<>(24);
        for (NodeReference value : new NodeReference[] {ALT, BAG, FIRST, HTML, LANG_STRING, LIST, NIL, OBJECT,
                PLAIN_LITERAL, PREDICATE, PROPERTY, REST, SEQ, STATEMENT, SUBJECT, TYPE, VALUE, XML_LITERAL }) {
            reversed.put(value.getIdentifier(), value);
        }
    }

    /**
     * Returns the sequence number that is encoded in an URL.
     *
     * @param url
     *            URL whose sequence number is to return
     * @return the sequence number, if any, {@code null} otherwise
     */
    public static Long sequenceNumberOf(String url) {
        if (!url.startsWith(SEQ_NO_PREFIX)) {
            return null;
        }
        BigInteger value;
        try {
            value = new BigInteger(url.substring(SEQ_NO_PREFIX_LENGTH));
        } catch (NumberFormatException e) {
            return null;
        }
        if (value.compareTo(BigInteger.valueOf(Node.FIRST_INDEX)) < 0) {
            throw new IndexOutOfBoundsException(value + " is below " + Node.FIRST_INDEX);
        }
        if (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ArithmeticException(value + " does not fit into long");
        }
        return value.longValue();
    }

    /**
     * Returns an URL that encodes the given sequence number in the RDF
     * namespace.
     *
     * @param nnn
     *            sequence number
     * @return an URL for the sequence number
     */
    public static String toURL(long nnn) {
        if (nnn < Node.FIRST_INDEX) {
            throw new IllegalArgumentException(nnn + " is below " + Node.FIRST_INDEX);
        }
        return SEQ_NO_PREFIX.concat(Long.toString(nnn));
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
     *
     *
     * /** Private constructor: You cannot create instance of this class.
     */
    private RDF() {
    }
}
