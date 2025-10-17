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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * A {@code <condition>}, which can be defined inside a {@code <restriction>}
 * and contains conditional {@code <permit>} rules, which only apply if the
 * condition is met.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Condition implements ConditionsMapInterface {
    /**
     * Key to which this (restriction) rule applies, or key that is allowed (for
     * permit rule inside restriction rule).
     */
    @XmlAttribute(required = true)
    private String key;

    /**
     * Value that the metadata entry must have for the condition to apply.
     */
    @XmlAttribute(required = true)
    private String equals;

    /**
     * List of conditional permits.
     */
    @XmlElement(name = "permit", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<RestrictivePermit> permits = new LinkedList<>();

    /**
     * List of (nested) conditions.
     */
    @XmlElement(name = "condition", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Condition> conditions = new LinkedList<>();

    private transient ConditionsMap conditionsMap;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEquals() {
        return equals;
    }

    public void setEquals(String equals) {
        this.equals = equals;
    }

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
     * Returns the (modifiable) conditions list. Used in rule merges.
     * @return the conditions list
     */
    public List<Condition> getConditions() {
        return conditions;
    }
}
