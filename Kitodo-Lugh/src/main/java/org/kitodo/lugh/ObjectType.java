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

import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

/**
 * A node, a reference to a node, or any kind of literal.
 *
 * @author Matthias Ronge
 */
public interface ObjectType {
    /**
     * This pattern can be used to check if a string is a valid URI.
     */
    static final Pattern URI_SCHEME = Pattern.compile("^[A-Za-z][+\\-.0-9A-Za-z]*:");

    /**
     * Converts this node type to an RDFNode as part of a Jena model.
     *
     * @param model
     *            model to create objects in
     * @return an RDFNode representing this node
     */
    RDFNode toRDFNode(Model model);
}
