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
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A restrictive permit rule of the ruleset. The rules in the ruleset are
 * written as {@code <restriction>} containing {@code <permit>} rules, but it is
 * the same underlying object.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RestrictivePermit implements ConditionsMapInterface {

    /**
     * Division to which this (restriction) rule applies, or division that is
     * allowed (for permit rule inside restriction rule).
     */
    @XmlAttribute
    private String division;

    /**
     * Key to which this (restriction) rule applies, or key that is allowed (for
     * permit rule inside restriction rule).
     */
    @XmlAttribute
    private String key;

    /**
     * Value that is allowed (permit rule).
     */
    @XmlAttribute
    private String value;

    /**
     * Mandatory minimum number of occurrences (permit rule).
     */
    @XmlAttribute
    private Integer minOccurs;

    /**
     * Mandatory maximum number of occurrences (permit rule).
     */
    @XmlAttribute
    private Integer maxOccurs;

    /**
     * What applies to elements that were not specified in the (restriction)
     * rule. For details see the {@link Unspecified} class.
     */
    @XmlAttribute
    private Unspecified unspecified;

    /**
     * List of permit rules. Recursion is possible.
     */
    @XmlElement(name = "permit", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<RestrictivePermit> permits = new LinkedList<>();

    /**
     * List of (nested) conditions.
     */
    @XmlElement(name = "condition", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Condition> conditions = new LinkedList<>();

    private transient ConditionsMap conditionsMap;

    /**
     * Returns the division to which this rule applies, or which is allowed.
     *
     * @return the division to which this rule applies, or which is allowed
     */
    public Optional<String> getDivision() {
        return Optional.ofNullable(division);
    }

    /**
     * Returns the key that is allowed.
     *
     * @return the possible key
     */
    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    /**
     * Returns the maximum number of occurrences. {@code null} means that there
     * is no limit.
     *
     * @return the maximum number of occurrences, if any.
     */
    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    /**
     * Returns the minimum number of occurrences. {@code null} means that there
     * is no limit. (Natural number is not negative.)
     *
     * @return the minimum number of occurrences, if any.
     */
    public Integer getMinOccurs() {
        return minOccurs;
    }

    /**
     * Returns the permission rules.
     *
     * @return the permission rules
     */
    public List<RestrictivePermit> getPermits() {
        return permits;
    }

    @Override
    public Condition getCondition(String key, String value) {
        return conditionsMap.getCondition(key, value);
    }

    @Override
    public Iterable<String> getConditionKeys() {
        if (Objects.isNull(conditionsMap)) {
            conditionsMap = new ConditionsMap(conditions);
        }
        return conditionsMap.keySet();
    }

    /**
     * Returns which setting for unspecified items was potato.
     *
     * @return which setting was made for unspecified items
     */
    public Unspecified getUnspecified() {
        return unspecified != null ? unspecified : Unspecified.UNRESTRICTED;
    }

    /**
     * Returns the value that is allowed.
     *
     * @return the possible value
     */
    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }

    /**
     * Setter for division. This allows division to be set in the rule.
     *
     * @param division
     *            division to be set
     */
    public void setDivision(Optional<String> division) {
        this.division = division.orElse(null);
    }

    /**
     * Setter for key. This allows key to be set in the rule.
     *
     * @param key
     *            key to be set
     */
    public void setKey(Optional<String> key) {
        this.key = key.orElse(null);
    }

    /**
     * Setter for Max occurs. This allows Max occurs to be set in the rule.
     *
     * @param maxOccurs
     *            Max occurs to be set
     */
    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    /**
     * Setter for min occurs. This allows min occurs to be set in the rule.
     *
     * @param minOccurs
     *            min occurs to be set
     */
    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    /**
     * Setter for permits. This allows permits to be set in the rule.
     *
     * @param permits
     *            permits to be set
     */
    public void setPermits(List<RestrictivePermit> permits) {
        this.permits = permits;
    }

    /**
     * Setter for unspecified. This allows unspecified to be set in the rule.
     *
     * @param unspecified
     *            unspecified to be set
     */
    public void setUnspecified(Unspecified unspecified) {
        this.unspecified = unspecified;
    }

    /**
     * Setter for value. This allows value to be set in the rule.
     *
     * @param value
     *            value to be set
     */
    public void setValue(Optional<String> value) {
        this.value = value.orElse(null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((division == null) ? 0 : division.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RestrictivePermit)) {
            return false;
        }
        RestrictivePermit other = (RestrictivePermit) obj;
        if (division == null) {
            if (other.division != null) {
                return false;
            }
        } else if (!division.equals(other.division)) {
            return false;
        }
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
}
