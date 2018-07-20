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

package org.kitodo.services.data;

import de.sub.goobi.config.ConfigCore;

import java.util.List;
import java.util.Objects;

import javax.json.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.RulesetDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.RulesetType;
import org.kitodo.data.elasticsearch.index.type.enums.DocketTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.RulesetTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.RulesetDTO;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.services.data.base.TitleSearchService;

public class RulesetService extends TitleSearchService<Ruleset, RulesetDTO, RulesetDAO> {

    private static final Logger logger = LogManager.getLogger(RulesetService.class);
    private static RulesetService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private RulesetService() {
        super(new RulesetDAO(), new RulesetType(), new Indexer<>(Ruleset.class), new Searcher(Ruleset.class));
    }

    /**
     * Return singleton variable of type RulesetService.
     *
     * @return unique instance of RulesetService
     */
    public static RulesetService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (RulesetService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new RulesetService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Ruleset");
    }

    /**
     * Find ruleset with exact file.
     *
     * @param file
     *            of the searched ruleset
     * @return search result
     */
    public JsonObject findByFile(String file) throws DataException {
        QueryBuilder queryBuilder = createSimpleQuery(RulesetTypeField.FILE.getName(), file, true);
        return searcher.findDocument(queryBuilder.toString());
    }

    /**
     * Find rulesets with exact file content.
     *
     * @param fileContent
     *            of the searched ruleset
     * @return list of JSON objects
     */
    public List<JsonObject> findByFileContent(String fileContent) throws DataException {
        QueryBuilder queryBuilder = createSimpleQuery(RulesetTypeField.FILE_CONTENT.getName(), fileContent, true);
        return searcher.findDocuments(queryBuilder.toString());
    }

    /**
     * Find rulesets for client id.
     *
     * @param clientId
     *            of the searched rulesets
     * @return search result
     */
    List<JsonObject> findByClientId(Integer clientId) throws DataException {
        QueryBuilder query = createSimpleQuery(DocketTypeField.CLIENT_ID.getName(), clientId, true);
        return searcher.findDocuments(query.toString());
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
    public JsonObject findByTitleAndFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(RulesetTypeField.TITLE.getName(), title, true, Operator.AND));
        query.must(createSimpleQuery(RulesetTypeField.FILE.getName(), file, true, Operator.AND));
        return searcher.findDocument(query.toString());
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
    public List<JsonObject> findByTitleOrFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery(RulesetTypeField.TITLE.getName(), title, true));
        query.should(createSimpleQuery(RulesetTypeField.FILE.getName(), file, true));
        return searcher.findDocuments(query.toString());
    }

    @Override
    public RulesetDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        RulesetDTO rulesetDTO = new RulesetDTO();
        rulesetDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject rulesetJSONObject = jsonObject.getJsonObject("_source");
        rulesetDTO.setTitle(RulesetTypeField.TITLE.getStringValue(rulesetJSONObject));
        rulesetDTO.setFile(RulesetTypeField.FILE.getStringValue(rulesetJSONObject));
        rulesetDTO.setOrderMetadataByRuleset(
            RulesetTypeField.ORDER_METADATA_BY_RULESET.getBooleanValue(rulesetJSONObject));
        return rulesetDTO;
    }

    /**
     * Get preferences.
     *
     * @param ruleset
     *            object
     * @return preferences
     */
    public PrefsInterface getPreferences(Ruleset ruleset) {
        PrefsInterface myPreferences = UghImplementation.INSTANCE.createPrefs();
        try {
            myPreferences.loadPrefs(ConfigCore.getParameter(Parameters.DIR_RULESETS) + ruleset.getFile());
        } catch (PreferencesException e) {
            logger.error(e.getMessage(), e);
        }
        return myPreferences;
    }
}
