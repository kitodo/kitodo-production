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

package org.kitodo.dataeditor.ruleset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalDivision;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Reimport;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.dataeditor.ruleset.xml.AcquisitionStage;
import org.kitodo.dataeditor.ruleset.xml.Division;
import org.kitodo.dataeditor.ruleset.xml.Key;
import org.kitodo.dataeditor.ruleset.xml.Namespace;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;
import org.kitodo.dataeditor.ruleset.xml.Setting;
import org.kitodo.utils.JAXBContextCache;

/**
 * This class provides the functionality of the rule set.
 */
public class RulesetManagement implements RulesetManagementInterface {
    /**
     * A logger can be used to keep a log.
     */
    private static final Logger logger = LogManager.getLogger(RulesetManagement.class);

    /**
     * English, the only language understood by the System user. This value is
     * passed when a method requests a language of the user in order to display
     * labels in this language, but the labels are not required from the result
     * and the language passed is therefore irrelevant at this point.
     */
    private static final List<LanguageRange> ENGLISH = LanguageRange.parse("en");

    /**
     * The ruleset.
     */
    private Ruleset ruleset;

    /**
     * Returns the acquisition levels defined in this rule set. This function
     * was not parallelized to repeatedly serve JSF in the same order when the
     * function is called repeatedly.
     *
     * @return all acquisition levels showing up
     */
    @Override
    public Collection<String> getAcquisitionStages() {
        List<AcquisitionStage> acquisitionStages = ruleset.getAcquisitionStages();
        List<String> acquisitionStageNames = new ArrayList<>(acquisitionStages.size());
        for (AcquisitionStage acquisitionStage : acquisitionStages) {
            acquisitionStageNames.add(acquisitionStage.getName());
        }
        return acquisitionStageNames;
    }

    @Override
    public List<String> getFunctionalKeys(FunctionalMetadata functionalMetadata) {
        return getIdsOfKeysForSpecialField(ruleset.getKeys(), functionalMetadata);
    }

    @Override
    public List<String> getFunctionalDivisions(FunctionalDivision functionalDivision) {
        return getIdsOfDivisionsForSpecialField(ruleset.getDivisions(), functionalDivision);
    }

    @Override
    public Collection<String> getDivisionsWithNoWorkflow() {
        Collection<DivisionDeclaration> divisionDeclarations = ruleset.getDivisionDeclarations(true, true);
        List<Division> divisions = divisionDeclarations.stream().map(DivisionDeclaration::getDivision)
                .collect(Collectors.toList());
        return getDivionsWithNoWorkflow(divisions);
    }

    private Collection<String> getDivionsWithNoWorkflow(List<Division> divisions) {
        ArrayList<String> divionsWithNoWorkflow = new ArrayList<>();
        for (Division division : divisions) {
            if (!division.isWithWorkflow()) {
                divionsWithNoWorkflow.add(division.getId());
            }
        }
        return divionsWithNoWorkflow;
    }

    private List<String> getIdsOfDivisionsForSpecialField(List<Division> divisions,
            FunctionalDivision functionalDivision) {
        ArrayList<String> idsOfDivisionsForSpecialField = new ArrayList<>();
        for (Division division : divisions) {
            if (Objects.isNull(division.getUse())) {
                continue;
            }
            Set<FunctionalDivision> uses = FunctionalDivision.valuesOf(division.getUse());
            if (uses.contains(functionalDivision)) {
                idsOfDivisionsForSpecialField.add(division.getId());
            }
        }
        return idsOfDivisionsForSpecialField;
    }

    private List<String> getIdsOfKeysForSpecialField(List<Key> keys, FunctionalMetadata functionalMetadata) {
        ArrayList<String> idsOfKeysForSpecialField = new ArrayList<>(1);
        for (Key key : keys) {
            if (key.getKeys().isEmpty()) {
                if (Objects.isNull(key.getUse())) {
                    continue;
                }
                Set<FunctionalMetadata> uses = FunctionalMetadata.valuesOf(key.getUse());
                if (uses.contains(functionalMetadata)) {
                    idsOfKeysForSpecialField.add(key.getId());
                }
            } else {
                List<String> idsOfKeysOfKey = getIdsOfKeysForSpecialField(key.getKeys(), functionalMetadata);
                for (String idOfKeyOfKey : idsOfKeysOfKey) {
                    idsOfKeysForSpecialField.add(key.getId() + '@' + idOfKeyOfKey);
                }
            }
        }
        return idsOfKeysForSpecialField;
    }

    /**
     * Returns a translated list of divisions available in the ruleset. The map
     * maps from ID to label.
     *
     * @return the map of divisions
     */
    @Override
    public Map<String, String> getStructuralElements(List<LanguageRange> priorityList) {
        return ruleset.getDivisions(priorityList, false, true);
    }

    /**
     * Opens a view on a division of the rule set.
     *
     * @param divisionId
     *            the division in view
     * @param acquisitionStage
     *            the current acquisition level
     * @param priorityList
     *            the wish list of the user regarding its preferred human
     *            languages
     * @return a view on a division
     */
    @Override
    public StructuralElementViewInterface getStructuralElementView(String divisionId, String acquisitionStage,
            List<LanguageRange> priorityList) {

        Optional<Division> division = ruleset.getDivision(divisionId);
        DivisionDeclaration divisionDeclaration = division.isPresent() ? new DivisionDeclaration(ruleset, division.get())
                : new DivisionDeclaration(ruleset, divisionId);
        return new DivisionView(ruleset, divisionDeclaration, acquisitionStage, priorityList);
    }

    /**
     * Opens a views on a metadata of the ruleset.
     *
     * @param keyId
     *          the id of the metadata
     * @param acquisitionStage
     *          the current acquisition level
     * @param priorityList
     *          the list of display languages preferred by the user
     * @return a view on a metadata
     */
    @Override
    public MetadataViewInterface getMetadataView(String keyId, String acquisitionStage, List<LanguageRange> priorityList) {
        Optional<Key> key = ruleset.getKey(keyId);
        KeyDeclaration keyDeclaration = key.map(value -> new KeyDeclaration(ruleset, value))
                .orElseGet(() -> new KeyDeclaration(ruleset, keyId));
        Rule rule = ruleset.getRuleForKey(keyId);
        if (keyDeclaration.isComplex()) {
            return new NestedKeyView<>(ruleset, keyDeclaration, rule, ruleset.getSettings(acquisitionStage), priorityList);
        } else {
            return new KeyView(keyDeclaration, rule, ruleset.getSettings(acquisitionStage), priorityList);
        }
    }

    /**
     * Returns the most appropriate label for a key, if there is one.
     *
     * @param key
     *            key whose label should be returned
     * @param priorityList
     *            weighted list of user-preferred display languages. Return
     *            value of the function {@link LanguageRange#parse(String)}.
     * @return the best-matching label, if any
     */
    @Override
    public Optional<String> getTranslationForKey(String key, List<LanguageRange> priorityList) {
        return getTranslationForKey(Collections.singletonList(key), priorityList);
    }

    @Override
    public Optional<String> getTranslationForKey(List<String> keys, List<LanguageRange> priorityList) {
        Optional<Key> optionalKey = ruleset.getKey(keys.get(0));
        if (optionalKey.isPresent()) {
            KeyDeclaration keyDeclaration = new KeyDeclaration(ruleset, optionalKey.get());
            for (int i = 1; i < keys.size(); i++) {
                keyDeclaration = keyDeclaration.getSubkeyDeclaration(keys.get(i));
            }
            String label = keyDeclaration.getLabel(priorityList);
            return Optional.of(label);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Loads a ruleset from a file.
     *
     * @param rulesetFile
     *            file to load
     * @throws IOException
     *             if something goes wrong when reading
     */
    @Override
    public void load(File rulesetFile) throws IOException {
        this.ruleset = read(rulesetFile);
        initializeNamespaces(ruleset.getKeys(), rulesetFile.getParentFile());
    }

    /**
     * Initializes the elements of namespaces if there is a corresponding file.
     *
     * @param keys
     *            the keys of the rule set (are processed recursively)
     * @param home
     *            the ruleset directory
     * @throws IOException
     *             if I/O fails
     */
    private void initializeNamespaces(List<Key> keys, File home) throws IOException {
        for (Key key : keys) {
            Optional<String> optionalNamespace = key.getNamespace();
            if (optionalNamespace.isPresent()) {
                String namespaceURI = optionalNamespace.get();
                File file = new File(home, namespaceURI.replaceFirst("^.*?/([^/]*?)[#/]?$", "$1").concat(".xml"));
                if (file.isFile()) {
                    try {
                        Namespace namespace = read(Namespace.class, file);
                        if (namespace.isAbout(namespaceURI)) {
                            key.setOptions(namespace.getOptions());
                        } else {
                            logger.debug(
                                "The file {} for the namespace {} declares an inappropriate namespace. (Check about.)",
                                file, namespaceURI);
                        }
                    } catch (IOException e) {
                        logger.debug("The file {} for the namespace {} cannot be parsed: {}", file, namespaceURI,
                            e.getMessage());
                    }
                } else {
                    logger.debug("The file {} for the namespace {} was not found or is unreadable.", file,
                        namespaceURI);
                }
            }
            // is applied recursively to the sub-elements
            initializeNamespaces(key.getKeys(), home);
        }
    }

    private static Ruleset read(File rulesetFile) throws IOException {
        Ruleset result = new Ruleset();
        Ruleset base = read(Ruleset.class, rulesetFile);
        for (String include : base.getIncludes()) {
            File includedFile = new File(rulesetFile.getParentFile(), include);
            Ruleset included = read(Ruleset.class, includedFile);
            result.addAll(included);
        }
        result.addAll(base);
        return result;
    }

    /**
     * Reads an object from a file. For this purpose, a marshal eliminator of
     * Java XML bindings is created, which eliminates the class of marshals and
     * creates it as a Java object. As an error, an I/O exception is thrown out
     * directly. Other marshal eliminator exceptions are packed in I/O
     * exceptions, so the interface is independent of the parser used.
     *
     * @param objectClass
     *            class of object to read
     * @param inputFile
     *            file to read from
     * @return the read object
     * @throws IOException
     *             if I/O fails
     */
    @SuppressWarnings("unchecked")
    private static <T> T read(Class<T> objectClass, File inputFile) throws IOException {
        try {
            return JAXBContextCache.getInstance().getUnmarshalled(objectClass, inputFile);
        } catch (JAXBException e) {
            /*
             * If the parser ran on an IOException, we can throw it out
             * directly, because that allows the method signature.
             */
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                /*
                 * Conversely, parser exceptions must be wrapped in an
                 * IOException because the method signature does not allow the
                 * parser exceptions.
                 */
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isAlwaysShowingForKey(String keyId) {
        Optional<Setting> optionalSetting = ruleset.getSettings().parallelStream()
                .filter(setting -> setting.getKey().equals(keyId)).findAny();
        if (optionalSetting.isPresent()) {
            return optionalSetting.get().isAlwaysShowing();
        }
        return false;
    }

    @Override
    public int updateMetadata(String division, Collection<Metadata> currentMetadata, String acquisitionStage,
            Collection<Metadata> updateMetadata) {

        Settings settings = ruleset.getSettings(acquisitionStage);
        Collection<MetadataViewInterface> allowedMetadata = getStructuralElementView(division, acquisitionStage,
            ENGLISH).getAllowedMetadata();
        Collection<ReimportMetadata> metadataForReimport = createListOfMetadataToMerge(currentMetadata, settings,
            allowedMetadata, updateMetadata);

        int sizeBefore = currentMetadata.size();
        currentMetadata.clear();
        for (ReimportMetadata metadataInReimport : metadataForReimport) {
            currentMetadata.addAll(metadataInReimport.get());
        }
        return currentMetadata.size() - sizeBefore;
    }

    @Override
    public Reimport getMetadataReimport(String metadataKey, String acquisitionStage) {
        Settings settings = ruleset.getSettings(acquisitionStage);
        return settings.getReimport(metadataKey);
    }

    private Collection<ReimportMetadata> createListOfMetadataToMerge(Collection<Metadata> currentMetadata,
            Settings settings, Collection<MetadataViewInterface> allowedMetadata, Collection<Metadata> updateMetadata) {
        HashMap<String, ReimportMetadata> unifying = new HashMap<>();
        for (Metadata metadata : currentMetadata) {
            unifying.computeIfAbsent(metadata.getKey(), ReimportMetadata::new).addToCurrentEntries(metadata);
        }
        for (Metadata metadata : updateMetadata) {
            unifying.computeIfAbsent(metadata.getKey(), ReimportMetadata::new).addToUpdateEntries(metadata);
        }
        Collection<ReimportMetadata> result = unifying.values();
        for (ReimportMetadata entry : result) {
            entry.setReimport(settings.getReimport(entry.getKey()));
        }
        for (MetadataViewInterface metadataView : allowedMetadata) {
            ReimportMetadata metadataToMerge = unifying.get(metadataView.getId());
            if (Objects.nonNull(metadataToMerge)) {
                metadataToMerge.setMaxOccurs(metadataView.getMaxOccurs());
            }
        }
        return result;
    }
}
