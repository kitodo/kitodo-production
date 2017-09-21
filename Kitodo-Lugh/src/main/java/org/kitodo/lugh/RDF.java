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

package org.kitodo.lugh;

import java.util.regex.Pattern;

import org.kitodo.lugh.mem.MemNodeReference;

/**
 * Elements of the {@code http://www.w3.org/1999/02/22-rdf-syntax-ns} namespace.
 *
 * @author Matthias Ronge
 */
public class RDF {
    /**
     * The RDF subject.
     */
    public static final NodeReference ABOUT = new MemNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#about");

    /**
     * The datatype of language-tagged string values
     */
    public static final NodeReference LANG_STRING = new MemNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString");

    /**
     * The RDF namespace.
     */
    public static final String NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns";

    /**
     * The empty list, with no items in it. If the rest of a list is nil then
     * the list has no more items in it.
     */
    public static final NodeReference NIL = new MemNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");

    /**
     * The object of the subject RDF statement.
     */
    public static final NodeReference OBJECT = new MemNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");

    /**
     * The class of plain (i.e. untyped) literal values, as used in RIF and OWL
     * 2
     */
    public static final NodeReference PLAIN_LITERAL = new MemNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral");

    /**
     * The predicate of the subject RDF statement.
     */
    public static final NodeReference PREDICATE = new MemNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");

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
     * The subject is an instance of a class.
     */
    public static final NodeReference TYPE = new MemNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

    /**
     * Idiomatic property used for structured values.
     */
    public static final NodeReference VALUE = new MemNodeReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");

    /**
     * The datatype of XML literal values.
     */
    public static final NodeReference XML_LITERAL = new MemNodeReference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");

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
        String ciphers = null;
        try {
            ciphers = url.substring(SEQ_NO_PREFIX_LENGTH);
            Long value = Long.valueOf(ciphers);
            return value >= Node.FIRST_INDEX ? value : null;
        } catch (NumberFormatException e) {
            if(Pattern.compile("[0-9]+").matcher(ciphers).matches()) {
                throw new ArithmeticException(ciphers + " does not fit into long");
            }
            return null;
        }
    }

    /**
     * Returns an URL that encodes the given sequence number in the RDF
     * namespace.
     *
     * @param n
     *            sequence number
     * @return an URL for the sequence number
     */
    public static String toURL(long n) {
        if (n < Node.FIRST_INDEX) {
            throw new IllegalArgumentException("For long: " + n);
        }
        return SEQ_NO_PREFIX.concat(Long.toString(n));
    }

    /**
     * Private constructor: You cannot create instances of this class.
     */
    private RDF() { }
}
