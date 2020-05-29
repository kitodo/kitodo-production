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
import java.util.Objects;
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
     * The sequence description to form a process title.
     */
    @XmlAttribute
    private String processTitle;

    /**
     * The key used to store the part of the date relevant to this division, to
     * which the described subtree dates.
     */
    @XmlAttribute
    private String dates;

    /**
     * The use of the division.
     */
    @XmlAttribute
    private String use;

    /**
     * If the division should use a workflow.
     */
    @XmlAttribute
    private Boolean withWorkflow;

    /**
     * The schema in which the part of the date relevant to this division is
     * stored. Apart from the dates built into Java and interpreted by the
     * runtime, there is still the special string “{@code yyyy/yyyy}”, which
     * stands for a double year, eg. an operation year that starts on a day
     * other than January 1. This works in conjunction with
     * {@link SubdivisionByDateElement#yearBegin}.
     */
    @XmlAttribute
    private String scheme;

    /**
     * Human-readable identifiers for the type. There can be several, depending
     * on the language.
     */
    @XmlElement(name = "label", namespace = "http://names.kitodo.org/ruleset/v2", required = true)
    private List<Label> labels = new LinkedList<>();

    /**
     * In this element, if there is, it is stored that the division of this
     * division is done by divisions, which map a calendar date.
     */
    @XmlElement(namespace = "http://names.kitodo.org/ruleset/v2")
    private SubdivisionByDateElement subdivisionByDate;

    /**
     * Returns the key used to store the part of the date relevant to this
     * division, to which the described subtree dates.
     *
     * @return the key used to store the date
     */
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
     * Returns the sequence description to form a process title.
     *
     * @return the key used to store the date
     */
    public Optional<String> getProcessTitle() {
        return Optional.ofNullable(processTitle);
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


    /**
     * Returns the use.
     *
     * @return the use
     */
    public String getUse() {
        return use;
    }

    /**
     * Returns if a workflow is used.
     *
     * @return if a workflow is used.
     */
    public boolean isWithWorkflow() {
     return Objects.isNull(withWorkflow) ? true : withWorkflow;
    }
}
