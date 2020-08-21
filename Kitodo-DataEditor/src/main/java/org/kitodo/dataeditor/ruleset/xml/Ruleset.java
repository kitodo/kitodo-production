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
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
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

    transient private List<Key> keys;

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
                .filter(division -> division.getId().equals(id)).findFirst();
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
     * @param all
     *            whether to return all divisions, or only those capable to form
     *            a process title
     * @param subdivisionsByDate
     *            whether subdivisions by date should be returned
     * @return all outline elements as map from IDs to labels
     */
    public Map<String, String> getDivisions(List<LanguageRange> priorityList, boolean all, boolean subdivisionsByDate) {
        Collection<UniversalDivision> universalDivisions = getUniversalDivisions(all, subdivisionsByDate);
        return all || !universalDivisions.isEmpty() ? Labeled.listByTranslatedLabel(this,
            universalDivisions, UniversalDivision::getId, UniversalDivision::getLabels, priorityList)
                : getDivisions(priorityList, true, subdivisionsByDate);
    }

    /**
     * get all universalDivisions as Collection.
     * @param all if all divisions should be respected.
     * @param subdivisionsByDate if subdivisionsByDate should be respected.
     * @return a collection of universalDivisions.
     */
    public Collection<UniversalDivision> getUniversalDivisions(boolean all, boolean subdivisionsByDate) {
        Collection<UniversalDivision> universalDivisions = new LinkedList<>();
        for (Division division : declaration.getDivisions()) {
            UniversalDivision universalDivision = new UniversalDivision(this, division);
            if (all || universalDivision.getProcessTitle().isPresent()) {
                universalDivisions.add(universalDivision);
            }
            if (subdivisionsByDate) {
                for (UniversalDivision subdivision : universalDivision.getUniversalDivisions()) {
                    if (all || subdivision.getProcessTitle().isPresent()) {
                        universalDivisions.add(subdivision);
                    }
                }
            }
        }
        return universalDivisions;
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
        if (Objects.isNull(keys)) {
            keys = defineMetsDivKeys(declaration.getKeys());
        }
        return keys;
    }

    /**
     * Hard define keys for METS attributes CONTENTIDS, ORDER, and ORDERLABEL.
     *
     * @param keys
     *            keys defined in the ruleset
     * @return completed definition
     */
    private static List<Key> defineMetsDivKeys(List<Key> keys) {
        defineKey(keys, "CONTENTIDS", Type.ANY_URI, new Label("METS content ID"), new Label("de", "METS-Inhalts-ID"));
        defineKey(keys, "LABEL", Type.STRING, new Label("METS label"), new Label("de", "METS-Beschriftung"));
        defineKey(keys, "ORDERLABEL", Type.STRING, new Label("METS order label"),
            new Label("de", "METS-Anordnungsbeschriftung"));
        return keys;
    }

    /**
     * Hard defines a key which represents a METS attribute
     * ({@code domain="mets:div"}).
     *
     * @param keys
     *            key definition list, may already contain the key. If so, the
     *            key is updated (if necessary), else it is added.
     * @param id
     *            ID of the key
     * @param type
     *            type of the key
     * @param labels
     *            labels for the key if it is not defined
     */
    private static void defineKey(List<Key> keys, String id, Type type, Label... labels) {
        Optional<Key> definition = keys.parallelStream().filter(key -> key.getId().equalsIgnoreCase(id)).findAny();
        Key key;
        if (definition.isPresent()) {
            key = definition.get();
        } else {
            key = new Key();
            Collections.addAll(key.getLabels(), labels);
            keys.add(key);
        }
        key.setId(id);
        /*
         * If the key type is string (broadest type), but the required type is
         * narrower, set it. Do not set it if the user explicitly defined a
         * narrower type in the ruleset file.
         */
        Type currentType = key.getType();
        if (Objects.equals(currentType, Type.STRING) && !Objects.equals(currentType, type)) {
            key.setType(type);
        }
        key.setDomain(Domain.METS_DIV);
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
        if (Objects.isNull(acquisitionStage)) {
            return settings;
        }
        Optional<AcquisitionStage> optionalAcquisitionStage = this.getAcquisitionStage(acquisitionStage);
        optionalAcquisitionStage.ifPresent(stage -> settings.merge(stage.getSettings()));
        return settings;
    }

    /**
     * Returns a fictitious metadata key for the rule set that contains all the
     * keys of the rule set.
     *
     * @return a fictitious metadata key for the rule set
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
        if (Objects.isNull(division)) {
            return new UniversalRule(this, this.getDivisionRestriction(""));
        } else {
            return new UniversalRule(this, this.getDivisionRestriction(division));
        }
    }
}
