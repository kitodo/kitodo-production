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

package org.kitodo.production.lugh.ld;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * A node, a reference to a node, or any kind of literal.
 *
 * @author Matthias Ronge
 */
public interface ObjectType {
    /**
     * Converts this node type to an RDFNode as part of a Jena model.
     *
     * @param model
     *            model to create objects in
     * @return an RDFNode representing this node
     */
    RDFNode toRDFNode(Model model);
}
