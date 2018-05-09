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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A linked data literal.
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
    static final Set<String> ALLOWED_RELATIONS = new HashSet<>(Arrays.asList(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/XML/1998/namespace#lang",
                      "http://www.w3.org/1999/02/22-rdf-syntax-ns#value"));

    /**
     * Three literal types, not including RDF.LANG_STRING, that may be passed to
     * the Literal constructor when creating a common literal.
     */
    /*
     * Constants from `RDF.java` cannot be used here because the constants are
     * MemoryNodeReference objects, thus a subclass of this class and hence
     * cannot yet be accessed at creation time of this class.
     */
    static final List<String> LITERAL_TYPES = Arrays
            .asList("http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral",
                                  "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML",
                                  "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");

    /**
     * Pattern to check whether a String starts with an URL scheme as specified
     * in RFC 1738.
     */
    static final Pattern SCHEME_TEST = Pattern.compile("[+\\-\\.0-9A-Za-z]+:[^ ]+");

    /**
     * Returns the value of this literal.
     *
     * @return the literal value
     */
    String getValue();
}
