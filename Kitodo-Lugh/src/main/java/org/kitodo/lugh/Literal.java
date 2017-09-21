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

import java.util.*;
import java.util.regex.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

/**
 * A linked data literal.
 *
 * @author Matthias Ronge
 */
public interface Literal extends AccessibleObject {

    /**
     * Three relations allowed on a node describing a literal.
     */
    /*
     * Constants from `RDF.java` cannot be used here because the constants are
     * MemNodeReference objects, thus a subclass of this class and hence cannot
     * yet be accessed at creation time of this class.
     */
    public static final Set<String> ALLOWED_RELATIONS = new HashSet<>(
            Arrays.asList(new String[] { "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                    "http://www.w3.org/XML/1998/namespace#lang", "http://www.w3.org/1999/02/22-rdf-syntax-ns#value" }));

    /**
     * Three literal types, not including RDF.LANG_STRING, that may be passed to
     * the Literal constructor when creating a common literal.
     */
    /*
     * Constants from `RDF.java` cannot be used here because the constants are
     * MemNodeReference objects, thus a subclass of this class and hence cannot
     * yet be accessed at creation time of this class.
     */
    public static final List<String> LITERAL_TYPES = Arrays
            .asList(new String[] { "http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral",
                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML",
                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral" });

    /**
     * Pattern to check whether a String starts with an URL scheme as specified
     * in RFC 1738.
     */
    public static final Pattern SCHEME_TEST = Pattern.compile("[+\\-\\.0-9A-Za-z]+:[^ ]+");

    public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";



    /**
     * Compares this Literal against another object for equality.
     */
    @Override
    public boolean equals(Object obj);

    /**
     * Returns the semantic web type of this literal.
     */
    @Override
    public String getType();

    /**
     * Returns the value of this literal.
     *
     * @return the literal value
     */
    public String getValue();

    /**
     * Returns a hash code of the Literal.
     */
    @Override
    public int hashCode();

    /**
     * Returns whether this literal is described by the condition node type.
     */
    @Override
    public boolean matches(ObjectType condition);

    /**
     * Converts this literal to an RDFNode as part of a Jena model.
     *
     * @param model
     *            model to create objects in
     * @return an RDFNode representing this node
     */
    @Override
    public RDFNode toRDFNode(Model model);
}
