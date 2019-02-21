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

package org.kitodo.dataeditor.ruleset.xml;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * A container for the properties of the XML element {@code <declaration>} in
 * the ruleset file.
 */
class DeclarationElement {
    /**
     * The declared divisions.
     */
    @XmlElement(name = "division", namespace = "http://names.kitodo.org/ruleset/v2", required = true)
    private List<Division> divisions = new LinkedList<>();

    /**
     * The declared keys.
     */
    @XmlElement(name = "key", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Key> keys = new LinkedList<>();

    /**
     * Returns the declared divisions.
     *
     * @return the declared divisions
     */
    List<Division> getDivisions() {
        return divisions;
    }

    /**
     * Returns the declared keys.
     *
     * @return the declared keys
     */
    List<Key> getKeys() {
        return keys;
    }
}
