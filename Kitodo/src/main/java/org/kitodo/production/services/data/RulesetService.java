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

package org.kitodo.production.services.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalDivision;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.RulesetDAO;
import org.kitodo.exceptions.RulesetNotFoundException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortOrder;

public class RulesetService extends BaseBeanService<Ruleset, RulesetDAO> {

    private static final Map<String, String> SORT_FIELD_MAPPING;

    static {
        SORT_FIELD_MAPPING = new HashMap<>();
        SORT_FIELD_MAPPING.put("title.keyword", "title");
        SORT_FIELD_MAPPING.put("file.keyword", "file");
        SORT_FIELD_MAPPING.put("orderMetadataByRuleset", "orderMetadataByRuleset");
    }

    private static final Logger logger = LogManager.getLogger(RulesetService.class);
    private static volatile RulesetService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private RulesetService() {
        super(new RulesetDAO());
    }

    /**
     * Return singleton variable of type RulesetService.
     *
     * @return unique instance of RulesetService
     */
    public static RulesetService getInstance() {
        RulesetService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (RulesetService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new RulesetService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM Ruleset");
    }

    @Override
    public Long countResults(Map<?, String> filtersNotImplemented) throws DAOException {
        BeanQuery beanQuery = new BeanQuery(Ruleset.class);
        beanQuery.restrictToClient(ServiceManager.getUserService().getSessionClientId());
        return count(beanQuery.formCountQuery(), beanQuery.getQueryParameters());
    }

    @Override
    public List<Ruleset> loadData(int first, int pageSize, String sortField, SortOrder sortOrder,
            Map<?, String> filtersNotImplemented) throws DAOException {
        BeanQuery beanQuery = new BeanQuery(Ruleset.class);
        beanQuery.restrictToClient(ServiceManager.getUserService().getSessionClientId());
        beanQuery.defineSorting(SORT_FIELD_MAPPING.getOrDefault(sortField, sortField), sortOrder);
        return getByQuery(beanQuery.formQueryForAll(), beanQuery.getQueryParameters(), first, pageSize);
    }

    /**
     * Returns all business domain models of the client, for which the logged in
     * user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The function requires that the thread is assigned to a logged-in user.
     * 
     * @return all dockets for the selected client
     */
    public List<Ruleset> getAllForSelectedClient() {
        return dao.getByQuery("SELECT r FROM Ruleset AS r INNER JOIN r.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    /**
     * Returns all business domain models with the specified label. This can be
     * used to check whether a label is still available.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * There is currently no filtering by client, so a label used by one client
     * cannot be used by another client.
     * 
     * @param title
     *            name to search for
     * @return list of dockets
     */
    public List<Ruleset> getByTitle(String title) {
        return dao.getByQuery("FROM Ruleset WHERE title = :title", Collections.singletonMap("title", title));
    }

    /**
     * Returns all rulesets with the given title and eagerly loads associated clients.
     *
     * @param title the title of the rulesets
     * @return list of rulesets with client fetched
     */
    public List<Ruleset> getByTitleWithClient(String title) {
        return dao.getByQuery(
                "SELECT r FROM Ruleset r JOIN FETCH r.client WHERE r.title = :title",
                Collections.singletonMap("title", title)
        );
    }

    /**
     * Get preferences.
     *
     * @param ruleset
     *            object
     * @return preferences
     */
    public LegacyPrefsHelper getPreferences(Ruleset ruleset) {
        LegacyPrefsHelper myPreferences = new LegacyPrefsHelper();
        try {
            myPreferences.loadPrefs(ConfigCore.getParameter(ParameterCore.DIR_RULESETS) + ruleset.getFile());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return myPreferences;
    }

    /**
     * Acquires a ruleset Management and loads a ruleset into it.
     *
     * @param ruleset
     *            database object that references the ruleset
     * @return a Ruleset Management in which the ruleset has been loaded
     */
    public RulesetManagementInterface openRuleset(Ruleset ruleset) throws IOException {
        final long begin = System.nanoTime();
        RulesetManagementInterface rulesetManagement = ServiceManager.getRulesetManagementService()
                .getRulesetManagement();
        String fileName = ruleset.getFile();
        try {
            rulesetManagement.load(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_RULESETS), fileName).toFile());
        } catch (FileNotFoundException | IllegalArgumentException e) {
            throw new RulesetNotFoundException(fileName);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Reading ruleset took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
        return rulesetManagement;
    }

    /**
     * Returns the names of those divisions that fulfill a given function.
     * 
     * @param rulesetId
     *            ruleset database number
     * @param function
     *            function that the divisions are supposed to fulfill
     * @return collection of identifiers for divisions that fulfill this
     *         function
     */
    public Collection<String> getFunctionalDivisions(Integer rulesetId, FunctionalDivision function) {
        try {
            Ruleset ruleset = ServiceManager.getRulesetService().getById(rulesetId);
            RulesetManagementInterface rulesetManagement;
            rulesetManagement = ServiceManager.getRulesetService().openRuleset(ruleset);
            return rulesetManagement.getFunctionalDivisions(function);
        } catch (DAOException | IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return Collections.emptySet();
        }
    }

    /**
     * Sort 'MetadataGroup' instance in given set 'metadataSet' by their nested functional metadata 'groupDisplayLabel'
     * if present and return resulting list.
     *
     * @param metadataSet set of metadata to be sorted by 'groupDisplayLabel'
     * @param ruleset Ruleset from which keys of functional metadata 'groupDisplayLabel' are retrieved
     * @return list of metadata instances of type 'MetadataGroup', filtered by their 'groupDisplayLabel'
     * @throws IOException if Ruleset could not be loaded
     */
    public static List<Metadata> getGroupsSortedByGroupDisplayLabel(HashSet<Metadata> metadataSet, Ruleset ruleset)
            throws IOException {
        Collection<String> groupDisplayLabel = ImportService.getGroupDisplayLabelMetadata(ruleset);
        return metadataSet.stream().filter(m -> m instanceof MetadataGroup)
                .sorted(Comparator.comparing(m -> ServiceManager.getRulesetService().getAnyNestedMetadataValue(m, groupDisplayLabel)))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve and return value of Metadata with any of the given 'keys' from given Ruleset 'ruleset'. If Metadata
     * contains no nested metadata with any of the given keys an empty String is returned instead.
     *
     * @param metadata Metadata for which value of nested metadata is retrieved
     * @param keys keys of nested metadata whose value is to be retrieved
     * @return value found of first nested metadata found, identified by provided keys
     */
    public String getAnyNestedMetadataValue(Metadata metadata, Collection<String> keys) {
        for (String groupDisplayLabelKey : keys) {
            String[] keySegments = groupDisplayLabelKey.split("@");
            String metadataValue = getNestedMetadataValue(metadata, Arrays.asList(keySegments).subList(1, keySegments.length));
            if (StringUtils.isNotBlank(metadataValue)) {
                return metadataValue;
            }
        }
        return "";
    }

    private String getNestedMetadataValue(Metadata metadata, List<String> keySegments) {
        if (metadata instanceof MetadataEntry) {
            return ((MetadataEntry)metadata).getValue();
        } else {
            if (Objects.isNull(keySegments) || keySegments.isEmpty()) {
                return "";
            }
            String currentSegment = keySegments.get(0);
            for (Metadata metadataElement : ((MetadataGroup)metadata).getMetadata()) {
                if (currentSegment.equals(metadataElement.getKey())) {
                    return getNestedMetadataValue(metadataElement, keySegments.subList(1, keySegments.size()));
                }
            }
        }
        return "";
    }

    /**
     * Retrieve translated label of MetadataEntry with key 'metadataEntryKey' nested in MetadataGroup with key
     * 'groupKey' from provided RulesetManagementInterface 'ruleset'.
     *
     * @param ruleset RulesetManagementInterface containing metadata rules
     * @param metadataEntryKey key of MetadataEntry in MetadataGroup for which translated label is returned
     * @param groupKey key of MetadataGroup
     * @param acquisitionStage Acquisition stage as String
     * @param languageRange preferred languages as list of LanguageRange
     * @return translated label of MetadataEntry nested in MetadataGroup
     */
    public String getMetadataEntryLabel(RulesetManagementInterface ruleset, String metadataEntryKey, String groupKey,
                                        String acquisitionStage, List<Locale.LanguageRange> languageRange) {
        MetadataViewInterface viewInterface = ruleset.getMetadataView(groupKey, acquisitionStage, languageRange);
        if (Objects.nonNull(viewInterface) && viewInterface instanceof ComplexMetadataViewInterface) {
            for (MetadataViewInterface metadataViewInterface : ((ComplexMetadataViewInterface)viewInterface).getAllowedMetadata()) {
                if (metadataEntryKey.equals(metadataViewInterface.getId())) {
                    return metadataViewInterface.getLabel();
                }
            }
        }
        return metadataEntryKey;
    }

    /**
     * Retrieve translated label of Metadata with key 'metadataKey' from RulesetManagementInterface 'ruleset'.
     *
     * @param ruleset RulesetManagementInterface containing metadata rules
     * @param metadataKey key of metadata for which translated label is returned
     * @param acquisitionStage Acquisition stage as String
     * @param languageRange preferred languages as list of LanguageRange
     * @return translated label of Metadata
     */
    public String getMetadataLabel(RulesetManagementInterface ruleset, String metadataKey, String acquisitionStage,
                                   List<Locale.LanguageRange> languageRange) {
        MetadataViewInterface viewInterface = ruleset.getMetadataView(metadataKey, acquisitionStage, languageRange);
        return viewInterface.getLabel();
    }
}
