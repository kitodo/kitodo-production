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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalDivision;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.RulesetDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.RulesetNotFoundException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.production.services.data.interfaces.DatabaseRulesetServiceInterface;
import org.primefaces.model.SortOrder;

public class RulesetService extends SearchDatabaseService<Ruleset, RulesetDAO>
        implements DatabaseRulesetServiceInterface {

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
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Ruleset");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        try {
            Map<String, Object> parameters = Collections.singletonMap("sessionClientId",
                ServiceManager.getUserService().getSessionClientId());
            return countDatabaseRows("SELECT COUNT(*) FROM Docket WHERE client_id = :sessionClientId", parameters);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    @Override
    public List<Ruleset> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sessionClientId", ServiceManager.getUserService().getSessionClientId());
        String desiredOrder = SORT_FIELD_MAPPING.get(sortField) + ' ' + SORT_ORDER_MAPPING.get(sortOrder);
        return getByQuery("FROM Ruleset WHERE client_id = :sessionClientId ORDER BY ".concat(desiredOrder), parameters,
            first, pageSize);
    }

    @Override
    public List<Ruleset> getAllForSelectedClient() {
        return dao.getByQuery("SELECT r FROM Ruleset AS r INNER JOIN r.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    /**
     * Get list of rulesets for given title.
     *
     * @param title
     *            for get from database
     * @return list of rulesets
     */
    @Override
    public List<Ruleset> getByTitle(String title) {
        return dao.getByQuery("FROM Ruleset WHERE title = :title", Collections.singletonMap("title", title));
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
}
