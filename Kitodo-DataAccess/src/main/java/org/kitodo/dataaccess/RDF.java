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

import java.math.BigInteger;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@code http://www.w3.org/1999/02/22-rdf-syntax-ns} namespace.
 */
public enum RDF implements NodeReference {
    /**
     * The class of containers of alternatives.
     */
    ALT("http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt"),

    /**
     * The class of unordered containers.
     */
    BAG("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag"),

    /**
     * The first item in the subject RDF list.
     */
    FIRST("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),

    /**
     * The datatype of RDF literals storing fragments of HTML content.
     */
    HTML("http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML"),

    /**
     * The datatype of language-tagged string values.
     */
    LANG_STRING("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"),

    /**
     * The class of RDF Lists.
     */
    LIST("http://www.w3.org/1999/02/22-rdf-syntax-ns#List"),

    /**
     * The empty list, with no items in it. If the rest of a list is nil then
     * the list has no more items in it.
     */
    NIL("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"),

    /**
     * The object of the subject RDF statement.
     */
    OBJECT("http://www.w3.org/1999/02/22-rdf-syntax-ns#object"),

    /**
     * The class of plain (i.e. untyped) literal values, as used in RIF and OWL
     * 2.
     */
    PLAIN_LITERAL("http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral"),

    /**
     * The predicate of the subject RDF statement.
     */
    PREDICATE("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"),

    /**
     * The class of RDF properties.
     */
    PROPERTY("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property"),

    /**
     * The rest of the subject RDF list after the first item.
     */
    REST("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),

    /**
     * The class of ordered containers.
     */
    SEQ("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq"),

    /**
     * The class of RDF statements.
     */
    STATEMENT("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement"),

    /**
     * The subject of the subject RDF statement.
     */
    SUBJECT("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject"),

    /**
     * The subject is an instance of a class.
     */
    TYPE("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),

    /**
     * Idiomatic property used for structured values.
     */
    VALUE("http://www.w3.org/1999/02/22-rdf-syntax-ns#value"),

    /**
     * The datatype of XML literal values.
     */
    XML_LITERAL("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");
    /**
     * Reserved string used to identify RDF subject in XML context.
     */
    public static final String ABOUT = "http://www.w3.org/1999/02/22-rdf-syntax-ns#about";

    /**
     * Reserved string used to express an RDF statement in XML context.
     */
    public static final String DESCRIPTION = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Description";

    private static final Logger logger = LogManager.getLogger(RDF.class);

    /**
     * The RDF namespace.
     */
    public static final String NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

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
     * Returns the sequence number that is encoded in an URL.
     *
     * @param url
     *            URL whose sequence number is to return
     * @return the sequence number, if any
     */
    public static Optional<Long> sequenceNumberOf(String url) {
        if (!url.startsWith(SEQ_NO_PREFIX)) {
            return Optional.empty();
        }
        BigInteger value;
        try {
            value = new BigInteger(url.substring(SEQ_NO_PREFIX_LENGTH));
        } catch (NumberFormatException e) {
            logger.warn((e.getMessage() != null ? e.getMessage() : "NumberFormatException") + " for URL " + url, e);
            return Optional.empty();
        }
        if (value.compareTo(BigInteger.valueOf(Node.FIRST_INDEX)) < 0) {
            throw new IndexOutOfBoundsException(value + " is below " + Node.FIRST_INDEX);
        }
        if (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ArithmeticException(value + " does not fit into long");
        }
        return Optional.of(value.longValue());
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

    private String identifier;

    /**
     * Creates a new NodeReference.
     *
     * @param identifier
     *            referenced URL
     */
    private RDF(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public RDFNode toRDFNode(Model model, Boolean unused) {
        return model.createResource(identifier);
    }

    /**
     * Returns a version of this node reference which, in a debugger, will
     * symbolically represent it.
     */
    @Override
    public String toString() {
        return '↗' + identifier;
    }
}
