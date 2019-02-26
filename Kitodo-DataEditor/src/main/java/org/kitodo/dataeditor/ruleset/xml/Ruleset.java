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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.kitodo.dataeditor.ruleset.Labeled;
import org.kitodo.dataeditor.ruleset.Settings;
import org.kitodo.dataeditor.ruleset.UniversalDivision;
import org.kitodo.dataeditor.ruleset.UniversalRule;

/**
 * This class maps the XML root element of the rule set. The ruleset is the
 * heart of Production. It consists of three sections: declaration, correlation
 * and editing.
 */
@XmlRootElement(name = "ruleset", namespace = "http://names.kitodo.org/ruleset/v2")
public class Ruleset {

    /**
     * The default language of the labels without language specification in the
     * rule set. Defaults to English.
     */
    @XmlAttribute
    private String lang;

    /**
     * The declaration section defines divisions and keys.
     */
    @XmlElement(name = "declaration", namespace = "http://names.kitodo.org/ruleset/v2")
    private DeclarationElement declaration = new DeclarationElement();

    /**
     * The correlation section defines relationships between divisions and keys.
     */
    @XmlElementWrapper(name = "correlation", namespace = "http://names.kitodo.org/ruleset/v2")
    @XmlElement(name = "restriction", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Rule> restrictions = new LinkedList<>();

    /**
     * In the editing section settings for the editor concerning keys are
     * defined.
     */
    @XmlElement(name = "editing", namespace = "http://names.kitodo.org/ruleset/v2")
    private EditingElement editing;

    /**
     * Returns an acquisition stage by name.
     *
     * @param name
     *            name of the acquisition stage to be searched
     * @return the acquisition stage, if there is one
     */
    public Optional<AcquisitionStage> getAcquisitionStage(String name) {
        if (editing == null) {
            return Optional.empty();
        }
        return editing.getAcquisitionStage(name);
    }

    /**
     * Returns all acquisition stages defined in the rule set.
     *
     * @return the list of the acquisition stages
     */
    public List<AcquisitionStage> getAcquisitionStages() {
        if (editing == null) {
            return Collections.emptyList();
        }
        return editing.getAcquisitionStages();
    }

    /**
     * Returns the default language of labels without a language in the ruleset.
     * Defaults to English.
     *
     * @return the default language of labels without a language
     */
    public String getDefaultLang() {
        return lang == null ? "en" : lang;
    }

    /**
     * Returns a division based on its identification.
     *
     * @param id
     *            Identification of the division to be searched
     * @return the division, if there is one
     */
    public Optional<Division> getDivision(String id) {
        Optional<Division> optionalDivision = declaration.getDivisions().parallelStream()
                .filter(division -> id.equals(division.getId())).findFirst();
        if (optionalDivision.isPresent()) {
            return optionalDivision;
        } else {
            return this.getDivisions().parallelStream().flatMap(division -> division.getDivisions().parallelStream())
                    .filter(division -> division.getId().equals(id)).findFirst();
        }
    }

    /**
     * Returns a restriction rule for a division, if any.
     *
     * @param division
     *            Division to search a restriction rule for
     * @return the restriction rule if there is one
     */
    public Optional<Rule> getDivisionRestriction(String division) {
        return restrictions.parallelStream().filter(rule -> division.equals(rule.getDivision().orElse(null)))
                .findFirst();
    }

    /**
     * Returns the total list of all divisions returned by the ruleset.
     *
     * @return all divisions of the rule set
     */
    public List<Division> getDivisions() {
        return declaration.getDivisions();
    }

    /**
     * Returns the divisions defined in this rule set.
     *
     * @param priorityList
     *            weighted list of user-preferred display languages. Return
     *            value of the function {@link LanguageRange#parse(String)}.
     * @param subdivisionsByDate
     *            whether subdivisions by date should be returned
     * @return all outline elements as map from IDs to labels
     */
    public Map<String, String> getDivisions(List<LanguageRange> priorityList, boolean subdivisionsByDate) {
        Collection<UniversalDivision> universalDivisions = new LinkedList<>();
        for (Division division : declaration.getDivisions()) {
            UniversalDivision universalDivision = new UniversalDivision(this, division);
            universalDivisions.add(universalDivision);
            if (subdivisionsByDate) {
                universalDivisions.addAll(universalDivision.getUniversalDivisions());
            }
        }
        return Labeled.listByTranslatedLabel(this, universalDivisions, UniversalDivision::getId,
            UniversalDivision::getLabels, priorityList);
    }

    /**
     * This will allow a key to come out of the ruleset.
     * 
     * @param keyId
     *            Identifier of the key
     * @return a key, if any
     */
    public Optional<Key> getKey(String keyId) {
        return declaration.getKeys().parallelStream().filter(key -> keyId.equals(key.getId())).findAny();
    }

    /**
     * So you can get the restriction on a key, if there is one.
     * 
     * @param keyId
     *            key for which the restriction is to be given
     * @return the restriction on a key, if any
     */
    public Optional<Rule> getKeyRestriction(String keyId) {
        return restrictions.parallelStream().filter(rule -> keyId.equals(rule.getKey().orElse(null))).findAny();
    }

    /**
     * Returns the total list of all keys returned by the ruleset.
     *
     * @return all keys of the rule set
     */
    public List<Key> getKeys() {
        return declaration.getKeys();
    }

    /**
     * Returns the total list of all settings returned by the ruleset.
     *
     * @return all settings of the rule set
     */
    public List<Setting> getSettings() {
        if (editing == null) {
            return Collections.emptyList();
        }
        return editing.getSettings();
    }

    /**
     * Returns the settings for an acquisition stage. That can be empty but it
     * works.
     *
     * @param acquisitionStage
     *            acquisition stage for which the settings is to be returned
     * @return settings for acquisition stage
     */
    public Settings getSettings(String acquisitionStage) {
        Settings settings = new Settings(this.getSettings());
        Optional<AcquisitionStage> optionalAcquisitionStage = this.getAcquisitionStage(acquisitionStage);
        if (optionalAcquisitionStage.isPresent()) {
            settings.merge(optionalAcquisitionStage.get().getSettings());
        }
        return settings;
    }

    /**
     * Returns a fictitious meta-data key for the rule set that contains all the
     * keys of the rule set.
     *
     * @return a fictitious meta-data key for the rule set
     */
    public Key getFictiousRulesetKey() {
        Key fictiousRulesetKey = new Key();
        fictiousRulesetKey.setKeys(declaration.getKeys());
        return fictiousRulesetKey;
    }

    /**
     * Returns a universal restriction rule for a key. That can be empty but it
     * works.
     *
     * @param keyId
     *            key for which a universal restriction rule is to be returned
     * @return universal restriction rule for key
     */
    public UniversalRule getUniversalRestrictionRuleForKey(String keyId) {
        return new UniversalRule(this, this.getKeyRestriction(keyId));
    }

    /**
     * Returns a universal restriction rule for a division. That can be empty but
     * it works.
     *
     * @param division
     *            division for which a universal restriction rule is to be
     *            returned
     * @return universal restriction rule for division
     */
    public UniversalRule getUniversalRestrictionRuleForDivision(String division) {
        return new UniversalRule(this, this.getDivisionRestriction(division));
    }
}
