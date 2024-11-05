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
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.RulesetType;
import org.kitodo.data.elasticsearch.index.type.enums.DocketTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.RulesetTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.RulesetNotFoundException;
import org.kitodo.production.dto.ClientDTO;
import org.kitodo.production.dto.RulesetDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.ClientSearchService;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.Operator;
import org.opensearch.index.query.QueryBuilder;
import org.primefaces.model.SortOrder;

public class RulesetService extends ClientSearchService<Ruleset, RulesetDTO, RulesetDAO> {

    private static final Logger logger = LogManager.getLogger(RulesetService.class);
    private static volatile RulesetService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private RulesetService() {
        super(new RulesetDAO(), new RulesetType(), new Indexer<>(Ruleset.class), new Searcher(Ruleset.class),
                RulesetTypeField.CLIENT_ID.getKey());
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
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Ruleset");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Ruleset WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(getRulesetsForCurrentUserQuery());
    }

    @Override
    public List<RulesetDTO> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        return findByQuery(getRulesetsForCurrentUserQuery(), getSortBuilder(sortField, sortOrder), first, pageSize,
            false);
    }

    @Override
    public List<Ruleset> getAllNotIndexed() {
        return getByQuery("FROM Ruleset WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Ruleset> getAllForSelectedClient() {
        return dao.getByQuery("SELECT r FROM Ruleset AS r INNER JOIN r.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public RulesetDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        RulesetDTO rulesetDTO = new RulesetDTO();
        rulesetDTO.setId(getIdFromJSONObject(jsonObject));
        rulesetDTO.setTitle(RulesetTypeField.TITLE.getStringValue(jsonObject));
        rulesetDTO.setFile(RulesetTypeField.FILE.getStringValue(jsonObject));
        rulesetDTO.setOrderMetadataByRuleset(
            RulesetTypeField.ORDER_METADATA_BY_RULESET.getBooleanValue(jsonObject));

        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(RulesetTypeField.CLIENT_ID.getIntValue(jsonObject));
        clientDTO.setName(RulesetTypeField.CLIENT_NAME.getStringValue(jsonObject));

        rulesetDTO.setClientDTO(clientDTO);
        return rulesetDTO;
    }

    /**
     * Get list of rulesets for given title.
     *
     * @param title
     *            for get from database
     * @return list of rulesets
     */
    public List<Ruleset> getByTitle(String title) {
        return dao.getByQuery("FROM Ruleset WHERE title = :title", Collections.singletonMap("title", title));
    }

    /**
     * Find ruleset with exact file.
     *
     * @param file
     *            of the searched ruleset
     * @return search result
     */
    public Map<String, Object> findByFile(String file) throws DataException {
        QueryBuilder queryBuilder = createSimpleQuery(RulesetTypeField.FILE.getKey(), file, true);
        return findDocument(queryBuilder);
    }

    /**
     * Find rulesets for client id.
     *
     * @param clientId
     *            of the searched rulesets
     * @return search result
     */
    List<Map<String, Object>> findByClientId(Integer clientId) throws DataException {
        QueryBuilder query = createSimpleQuery(DocketTypeField.CLIENT_ID.getKey(), clientId, true);
        return findDocuments(query);
    }

    /**
     * Find ruleset with exact title and file name.
     *
     * @param title
     *            of the searched ruleset
     * @param file
     *            of the searched ruleset
     * @return search result
     */
    public Map<String, Object> findByTitleAndFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(RulesetTypeField.TITLE.getKey(), title, true, Operator.AND));
        query.must(createSimpleQuery(RulesetTypeField.FILE.getKey(), file, true, Operator.AND));
        return findDocument(query);
    }

    /**
     * Find ruleset with exact title or file name.
     *
     * @param title
     *            of the searched ruleset
     * @param file
     *            of the searched ruleset
     * @return search result
     */
    public List<Map<String, Object>> findByTitleOrFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery(RulesetTypeField.TITLE.getKey(), title, true));
        query.should(createSimpleQuery(RulesetTypeField.FILE.getKey(), file, true));
        return findDocuments(query);
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

    private QueryBuilder getRulesetsForCurrentUserQuery() {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(RulesetTypeField.CLIENT_ID.getKey(),
                ServiceManager.getUserService().getSessionClientId(), true));
        return query;
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
        ComplexMetadataViewInterface viewInterface = ruleset.getMetadataView(groupKey, acquisitionStage, languageRange);
        if (Objects.nonNull(viewInterface)) {
            for (MetadataViewInterface metadataViewInterface : viewInterface.getAllowedMetadata()) {
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
