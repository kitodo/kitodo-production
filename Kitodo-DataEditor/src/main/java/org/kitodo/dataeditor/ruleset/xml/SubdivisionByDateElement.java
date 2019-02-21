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

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A container for the properties of the XML element {@code <subdivisionByDate>}
 * in the ruleset file.
 */
class SubdivisionByDateElement {
    /**
     * Annual start of operation year.
     */
    @XmlAttribute()
    private String yearBegin;

    /**
     * Divisions to structure the temporal course.
     */
    @XmlElement(name = "division", namespace = "http://names.kitodo.org/ruleset/v2", required = true)
    private List<Division> divisions;

    List<Division> getDivisions() {
        return divisions;
    }

    String getYearBegin() {
        return yearBegin;
    }
}
