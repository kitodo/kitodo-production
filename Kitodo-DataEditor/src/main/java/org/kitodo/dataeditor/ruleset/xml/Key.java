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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.kitodo.api.dataeditor.rulesetmanagement.Domain;

/**
 * This key contains the data format description for a meta-data entry. You can
 * create the keys yourself in the rule set.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Key {
    /**
     * The internal key identifier.
     */
    @XmlAttribute(required = true)
    private String id;

    /**
     * The domain of the key.
     */
    @XmlAttribute
    private Domain domain;

    /**
     * The labels of the key in different languages.
     */
    @XmlElement(name = "label", namespace = "http://names.kitodo.org/ruleset/v2", required = true)
    private List<Label> labels = new LinkedList<>();

    /**
     * The codomain of the key.
     */
    @XmlElement(namespace = "http://names.kitodo.org/ruleset/v2")
    private CodomainElement codomain;

    /**
     * The options of select lists.
     */
    @XmlElement(name = "option", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Option> options = new LinkedList<>();

    /**
     * A pattern.
     */
    @XmlElement(namespace = "http://names.kitodo.org/ruleset/v2")
    private String pattern;

    /**
     * Preset values.
     */
    @XmlElement(name = "preset", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<String> presets = new LinkedList<>();

    /**
     * The keys in the key, for nesting keys.
     */
    @XmlElement(name = "key", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Key> keys = new LinkedList<>();

    /**
     * Returns the domain of the key.
     * 
     * @return the domain
     */
    public Domain getDomain() {
        return domain;
    }

    /**
     * Returns the ID of the relation.
     * 
     * @return the ID of the relation
     */
    public String getId() {
        return id;
    }

    /**
     * Returns key relations from the target of the relation.
     * 
     * @return key relations from the target of the relation
     */
    public List<Key> getKeys() {
        return keys;
    }

    /**
     * Returns the slats.
     * 
     * @return the slats
     */
    public List<Label> getLabels() {
        return labels;
    }

    /**
     * Returns the namespace, if one has been set.
     * 
     * @return the namespace, if annie
     */
    public Optional<String> getNamespace() {
        if (codomain == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(codomain.getNamespace());
    }

    /**
     * Optionally returns a list.
     * 
     * @return a list
     */
    public List<Option> getOptions() {
        return options;
    }

    /**
     * Returns the pattern.
     * 
     * @return the knitting pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns the presents.
     * 
     * @return the presents
     */
    public List<String> getPresets() {
        return presets;
    }

    /**
     * Returns the type.
     * 
     * @return the type
     */
    public Type getType() {
        if (codomain == null) {
            return Type.STRING;
        }
        if (codomain.getType() != null) {
            return codomain.getType();
        }
        return codomain.getNamespace() != null ? Type.ANY_URI : Type.STRING;
    }

    /**
     * Setter for all options.
     * 
     * @param options
     *            all options to be set
     */
    public void setOptions(Collection<Option> options) {
        this.options = new ArrayList<>(options);
    }

    /**
     * Setter for all keys.
     * 
     * @param keys
     *            all keys to be set
     */
    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }
}
