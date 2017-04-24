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

import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.parser.ParseException;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.RulesetDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.RulesetType;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.data.base.TitleSearchService;

import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;

public class RulesetService extends TitleSearchService {

    private static final Logger logger = Logger.getLogger(RulesetService.class);

    private RulesetDAO rulesetDao = new RulesetDAO();
    private RulesetType rulesetType = new RulesetType();
    private Indexer<Ruleset, RulesetType> indexer = new Indexer<>(Ruleset.class);

    /**
     * Constructor with searcher's assigning.
     */
    public RulesetService() {
        super(new Searcher(Ruleset.class));
    }

    /**
     * Method saves object to database and insert document to the index of
     * Elastic Search.
     *
     * @param ruleset
     *            object
     */
    public void save(Ruleset ruleset) throws CustomResponseException, DAOException, IOException {
        rulesetDao.save(ruleset);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(ruleset, rulesetType);
    }

    public Ruleset find(Integer id) throws DAOException {
        return rulesetDao.find(id);
    }

    public List<Ruleset> findAll() throws DAOException {
        return rulesetDao.findAll();
    }

    public List<Ruleset> search(String query) throws DAOException {
        return rulesetDao.search(query);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param ruleset
     *            object
     */
    public void remove(Ruleset ruleset) throws CustomResponseException, DAOException, IOException {
        rulesetDao.remove(ruleset);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(ruleset, rulesetType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws CustomResponseException, DAOException, IOException {
        rulesetDao.remove(id);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(id);
    }

    /**
     * Find ruleset with exact file.
     *
     * @param file
     *            of the searched ruleset
     * @return search result
     */
    public SearchResult findByFile(String file) throws CustomResponseException, IOException, ParseException {
        QueryBuilder queryBuilder = createSimpleQuery("file", file, true);
        return searcher.findDocument(queryBuilder.toString());
    }

    /**
     * Find rulesets with exact file content.
     *
     * @param fileContent
     *            of the searched ruleset
     * @return list of search results
     */
    public List<SearchResult> findByFileContent(String fileContent)
            throws CustomResponseException, IOException, ParseException {
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
    public SearchResult findByTitleAndFile(String title, String file)
            throws CustomResponseException, IOException, ParseException {
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
    public List<SearchResult> findByTitleOrFile(String title, String file)
            throws CustomResponseException, IOException, ParseException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery("title", title, true));
        query.should(createSimpleQuery("file", file, true));
        return searcher.findDocuments(query.toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws CustomResponseException, DAOException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), rulesetType);
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
