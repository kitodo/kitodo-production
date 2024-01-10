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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.dataeditor.ruleset.DivisionDeclaration;
import org.kitodo.dataeditor.ruleset.Labeled;
import org.kitodo.dataeditor.ruleset.Rule;
import org.kitodo.dataeditor.ruleset.Settings;

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

    @XmlElement(name = "include", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<String> includes = new ArrayList<>();

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
    private List<RestrictivePermit> restrictions = new LinkedList<>();

    /**
     * In the editing section settings for the editor concerning keys are
     * defined.
     */
    @XmlElement(name = "editing", namespace = "http://names.kitodo.org/ruleset/v2")
    private EditingElement editing;

    private transient List<Key> keys;

    /**
     * Inserts all information from another ruleset into this ruleset. Information
     * of the same name will be overwritten.
     *
     * @param other ruleset to insert
     */
    public void addAll(Ruleset other) {
        if (Objects.nonNull(other.declaration)) {
            if (Objects.isNull(declaration)) {
                declaration = other.declaration;
            } else {
                replaceOrAdd(other.declaration.getDivisions(), Division::getId, declaration.getDivisions());
                replaceOrAdd(other.declaration.getKeys(), Key::getId, declaration.getKeys());
            }
        }
        if (Objects.nonNull(other.restrictions)) {
            if (Objects.isNull(restrictions)) {
                restrictions = other.restrictions;
            } else {
                replaceOrAdd(other.restrictions, RestrictivePermit::getKey, restrictions);
                replaceOrAdd(other.restrictions, RestrictivePermit::getDivision, restrictions);
            }
        }
        if (Objects.nonNull(other.editing)) {
            if (Objects.isNull(editing)) {
                editing = other.editing;
            } else {
                replaceOrAdd(other.editing.getSettings(), Setting::getKey, editing.getSettings());
                replaceOrAdd(other.editing.getAcquisitionStages(), AcquisitionStage::getName, editing.getAcquisitionStages());
            }
        }
    }

    private static <I, T> void replaceOrAdd(List<T> data, Function<T, I> getId, List<T> collector) {
        for (T entry : data) {
            I id = getId.apply(entry);
            if (Objects.equals(id, Optional.empty())) {
                continue;
            }
            boolean add = true;
            ListIterator<T> collectorIterator = collector.listIterator();
            while (collectorIterator.hasNext()) {
                if (Objects.equals(id, getId.apply(collectorIterator.next()))) {
                    collectorIterator.set(entry);
                    add = false;
                    break;
                }
            }
            if (add) {
                collector.add(entry);
            }
        }
    }

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
    public Optional<RestrictivePermit> getDivisionRestriction(String division) {
        return restrictions.parallelStream()
                .filter(restriction -> division.equals(restriction.getDivision().orElse(null)))
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
        Collection<DivisionDeclaration> divisionDeclarations = getDivisionDeclarations(all, subdivisionsByDate);
        return all || !divisionDeclarations.isEmpty() ? Labeled.listByTranslatedLabel(this,
            divisionDeclarations, DivisionDeclaration::getId, DivisionDeclaration::getLabels, priorityList)
                : getDivisions(priorityList, true, subdivisionsByDate);
    }

    /**
     * Returns all division declarations as a collection.
     *
     * @param all
     *            if true, returns all division declarations; if false, only
     *            returns division declarations with {@code processTitle}
     *            attribute set, which indicates that they are candidates for
     *            the logical root type, which typically corresponds to the
     *            media format
     * @param subdivisionsByDate
     *            if subdivisions by date should be included
     * @return a collection of division declarations
     */
    public Collection<DivisionDeclaration> getDivisionDeclarations(boolean all, boolean subdivisionsByDate) {
        Collection<DivisionDeclaration> divisionDeclarations = new LinkedList<>();
        for (Division division : declaration.getDivisions()) {
            DivisionDeclaration divisionDeclaration = new DivisionDeclaration(this, division);
            if (all || divisionDeclaration.getProcessTitle().isPresent()) {
                divisionDeclarations.add(divisionDeclaration);
            }
            if (subdivisionsByDate) {
                for (DivisionDeclaration subdivision : divisionDeclaration.getAllowedDivisionDeclarations()) {
                    if (all || subdivision.getProcessTitle().isPresent()) {
                        divisionDeclarations.add(subdivision);
                    }
                }
            }
        }
        return divisionDeclarations;
    }

    /**
     * Returns the filenames of the included rulesets.
     *
     * @return filenames of the included rulesets
     */
    public List<String> getIncludes() {
        return includes;
    }

    /**
     * Returns a key from the ruleset, if defined.
     *
     * @param keyId
     *            Identifier of the key
     * @return a key, if any
     */
    public Optional<Key> getKey(String keyId) {
        return declaration.getKeys().parallelStream().filter(key -> keyId.equals(key.getId())).findAny();
    }

    /**
     * Returns the restriction on a key, if there is one.
     *
     * @param keyId
     *            key for which the restriction is to be given
     * @return the restriction on a key, if any
     */
    public Optional<RestrictivePermit> getKeyRestriction(String keyId) {
        return restrictions.parallelStream().filter(restriction -> keyId.equals(restriction.getKey().orElse(null)))
                .findAny();
    }

    /**
     * Returns the complete list of all keys in this ruleset.
     *
     * @return all keys in this ruleset
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
     * Returns a rule for a key. The rule may be empty.
     *
     * @param keyId
     *            key for which a rule is to be returned
     * @return rule for the key
     */
    public Rule getRuleForKey(String keyId) {
        return new Rule(this, this.getKeyRestriction(keyId));
    }

    /**
     * Returns a rule for a division. The rule may be empty.
     *
     * @param division
     *            division for which a rule is to be returned
     * @return rule for division
     */
    public Rule getRuleForDivision(String division) {
        if (Objects.isNull(division)) {
            return new Rule(this, this.getDivisionRestriction(""));
        } else {
            return new Rule(this, this.getDivisionRestriction(division));
        }
    }
}
