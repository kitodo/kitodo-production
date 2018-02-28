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

package org.kitodo.dataaccess.format.xml;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.kitodo.dataaccess.NodeReference;

/**
 * The {@code http://www.w3.org/XML/1998/namespace} namespace.
 *
 * @see "http://www.w3.org/XML/1998/namespace"
 */
public enum XML implements NodeReference {
    /**
     * A facility for defining base URIs for processing relative URI references
     * is XML documents.
     *
     * @see "http://www.w3.org/TR/xmlbase/"
     */
    BASE("http://www.w3.org/XML/1998/namespace#base"),

    /**
     * An attribute known to be of type ID that can be used independently of any
     * DTD or schema.
     *
     * @see "http://www.w3.org/TR/xml-id/"
     */
    ID("http://www.w3.org/XML/1998/namespace#id"),

    /**
     * Identifies the human language of the subject as a RFC 4646 code.
     */
    LANG("http://www.w3.org/XML/1998/namespace#lang"),

    /**
     * Expresses whether or not the wishes white space is to be considered as
     * significant in the scope of the subject.
     */
    SPACE("http://www.w3.org/XML/1998/namespace#space");

    /**
     * The namespace for "xml:".
     */
    public static final String NAMESPACE = "http://www.w3.org/XML/1998/namespace#";

    private String identifier;

    /**
     * Enum constants constructor.
     *
     * @param identifier
     *            referenced URL
     */
    XML(String identifier) {
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
