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

import java.util.Locale;
import java.util.regex.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.kitodo.lugh.mem.MemNodeReference;

/**
 * An RDF lang string, that is a linked data literal with a language tag
 * attached.
 *
 * @author Matthias Ronge
 */
public interface LangString extends Literal {
    /**
     * Identifies the human language of the subject as a RFC 4646 code.
     */
    public static final NodeReference XML_LANG = new MemNodeReference("http://www.w3.org/XML/1998/namespace#lang");

    /**
     * Compares two objects for equality.
     */
    @Override
    public boolean equals(Object obj);

    /**
     * Returns a hash code for the object.
     */
    @Override
    public int hashCode();

    /**
     * Returns whether this LangString is described by the condition node type.
     */
    @Override
    public boolean matches(ObjectType condition);

    /**
     * Converts this lang string to an RDFNode as part of a Jena model.
     *
     * @param model
     *            model to create objects in
     * @return an RDFNode representing this node
     */
    @Override
    public RDFNode toRDFNode(Model model);
}
