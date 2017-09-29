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

import com.sun.research.ws.wadl.HTTPMethods;

import de.sub.goobi.config.ConfigCore;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.RulesetDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.RulesetType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.RulesetDTO;
import org.kitodo.services.data.base.TitleSearchService;

import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;

public class RulesetService extends TitleSearchService<Ruleset, RulesetDTO, RulesetDAO> {

    private static final Logger logger = LogManager.getLogger(RulesetService.class);

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public RulesetService() {
        super(new RulesetDAO(), new RulesetType(), new Indexer<>(Ruleset.class), new Searcher(Ruleset.class));
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Ruleset");
    }

    /**
     * Find ruleset with exact file.
     *
     * @param file
     *            of the searched ruleset
     * @return search result
     */
    public JSONObject findByFile(String file) throws DataException {
        QueryBuilder queryBuilder = createSimpleQuery("file", file, true);
        return searcher.findDocument(queryBuilder.toString());
    }

    /**
     * Find rulesets with exact file content.
     *
     * @param fileContent
     *            of the searched ruleset
     * @return list of JSON objects
     */
    public List<JSONObject> findByFileContent(String fileContent) throws DataException {
        QueryBuilder queryBuilder = createSimpleQuery("fileContent", fileContent, true);
        return searcher.findDocuments(queryBuilder.toString());
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
    public JSONObject findByTitleAndFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("title", title, true, Operator.AND));
        query.must(createSimpleQuery("file", file, true, Operator.AND));
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
    public List<JSONObject> findByTitleOrFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery("title", title, true));
        query.should(createSimpleQuery("file", file, true));
        return searcher.findDocuments(query.toString());
    }

    @Override
    public RulesetDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        RulesetDTO rulesetDTO = new RulesetDTO();
        rulesetDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject rulesetJSONObject = getSource(jsonObject);
        rulesetDTO.setTitle(getStringPropertyForDTO(rulesetJSONObject, "title"));
        rulesetDTO.setFile(getStringPropertyForDTO(rulesetJSONObject, "file"));
        return rulesetDTO;
    }

    /**
     * Get preferences.
     *
     * @param ruleset
     *            object
     * @return preferences
     */
    public Prefs getPreferences(Ruleset ruleset) {
        Prefs myPreferences = new Prefs();
        try {
            myPreferences.loadPrefs(ConfigCore.getParameter("RegelsaetzeVerzeichnis") + ruleset.getFile());
        } catch (PreferencesException e) {
            logger.error(e);
        }
        return myPreferences;
    }
}
