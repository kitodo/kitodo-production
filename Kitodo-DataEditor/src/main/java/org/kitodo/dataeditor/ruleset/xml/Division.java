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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A division is a possible type of element of the tree-shaped structure of the
 * digital representation of the cultural work.
 */
public class Division {
    /**
     * The internal identifier of the division.
     */
    @XmlAttribute(required = true)
    private String id;

    /**
     * The key used to store the part of the date relevant to this division, to
     * which the described subtree dates.
     */
    @XmlAttribute(required = true)
    private String dates;

    /**
     * The schema in which the part of the date relevant to this division is
     * stored. Apart from the dates built into Java and interpreted by the
     * runtime, there is still the special string “{@code yyyy/yyyy}”, which
     * stands for a double year, eg. an operation year that starts on a day
     * other than January 1. This works in conjunction with
     * {@link SubdivisionByDateElement#yearBegin}.
     */
    @XmlAttribute(required = true)
    private String scheme;

    /**
     * Human-readable identifiers for the type. There can be several, depending
     * on the language.
     */
    @XmlElement(name = "label", required = true)
    private List<Label> labels = new LinkedList<>();

    /**
     * In this element, if there is, it is stored that the division of this
     * division is done by divisions, which map a calendar date.
     */
    @XmlElement
    private SubdivisionByDateElement subdivisionByDate;

    public Optional<String> getDates() {
        return Optional.ofNullable(dates);
    }

    /**
     * Returns of divisions in it.
     *
     * @return divisions
     */
    public List<Division> getDivisions() {
        if (subdivisionByDate == null) {
            return Collections.emptyList();
        } else {
            return subdivisionByDate.getDivisions();
        }
    }

    /**
     * Returns the internal identifier of the division.
     *
     * @return the internal identifier of the division
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the human-readable identifiers for the type.
     *
     * @return the human-readable identifiers for the type
     */
    public List<Label> getLabels() {
        return labels;
    }

    /**
     * Returns the scheme of the date.
     * 
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns the begin of the operating year.
     * 
     * @return the begin of the operating year
     */
    public String getYearBegin() {
        if (subdivisionByDate == null) {
            return null;
        } else {
            return subdivisionByDate.getYearBegin();
        }
    }
}
