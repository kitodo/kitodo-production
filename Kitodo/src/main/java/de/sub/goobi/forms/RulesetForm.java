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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.model.LazyDTOModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.RulesetDTO;
import org.kitodo.services.ServiceManager;

@Named("RulesetForm")
@SessionScoped
public class RulesetForm extends BasisForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Ruleset ruleset;
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(RulesetForm.class);
    private int rulesetId;

    private LazyDTOModel lazyDTOModel = new LazyDTOModel(serviceManager.getRulesetService());

    public LazyDTOModel getLazyDTOModel() {
        return lazyDTOModel;
    }

    public void setLazyDTOModel(LazyDTOModel lazyDTOModel) {
        this.lazyDTOModel = lazyDTOModel;
    }

    /**
     * Initialize new Ruleset.
     *
     * @return page
     */
    public String createNewRuleset() {
        this.ruleset = new Ruleset();
        this.rulesetId = 0;
        return "/pages/RegelsaetzeBearbeiten?faces-redirect=true";
    }

    /**
     * Save.
     *
     * @return page or empty String
     */
    public String saveRuleset() {
        try {
            if (hasValidRulesetFilePath(this.ruleset, ConfigCore.getParameter("RegelsaetzeVerzeichnis"))) {
                serviceManager.getRulesetService().save(this.ruleset);
                return noFiltering();
            } else {
                Helper.setFehlerMeldung("RulesetNotFound");
                return null;
            }
        } catch (DataException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
            logger.error(e);
            return null;
        }
    }

    /**
     * Checks that ruleset file exists.
     *
     * @param ruleset
     *            ruleset
     * @param pathToRulesets
     *            path to ruleset
     * @return true if ruleset file exists
     */
    private boolean hasValidRulesetFilePath(Ruleset ruleset, String pathToRulesets) {
        File rulesetFile = new File(pathToRulesets + ruleset.getFile());
        return rulesetFile.exists();
    }

    /**
     * Remove.
     *
     * @return page or empty String
     */
    public String removeRuleset() {
        try {
            if (hasAssignedProcesses(this.ruleset)) {
                Helper.setFehlerMeldung("RulesetInUse");
                return null;
            } else {
                serviceManager.getRulesetService().remove(this.ruleset);
            }
        } catch (DataException e) {
            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
            return null;
        }
        return noFiltering();
    }

    private boolean hasAssignedProcesses(Ruleset ruleset) throws DataException {
        Integer number = serviceManager.getProcessService().findByRuleset(ruleset).size();
        return number > 0;
    }

    /**
     * No filtering.
     *
     * @return page or empty String
     */
    public String noFiltering() {
        List<RulesetDTO> rulesets = new ArrayList<>();
        try {
            rulesets = serviceManager.getRulesetService().findAll();
        } catch (DataException e) {
            logger.error(e);
        }
        this.page = new Page<>(0, rulesets);
        return "/pages/RegelsaetzeAlle?faces-redirect=true";
    }

    /**
     * This method initializes the ruleset list without applying any filters
     * whenever the bean is constructed.
     */
    @PostConstruct
    public void initializeRulesetList() {
        noFiltering();
    }

    public String filterKeinMitZurueck() {
        noFiltering();
        return this.zurueck;
    }

    /**
     * Method being used as viewAction for ruleset edit form. If 'rulesetId' is
     * '0', the form for creating a new ruleset will be displayed.
     */
    public void loadRuleset() {
        try {
            if (!Objects.equals(this.rulesetId, 0)) {
                setRuleset(this.serviceManager.getRulesetService().getById(this.rulesetId));
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error retrieving ruleset with ID '" + this.rulesetId + "'; ", e.getMessage());
        }
    }

    /*
     * Getter und Setter
     */

    public Ruleset getRuleset() {
        return this.ruleset;
    }

    public void setRuleset(Ruleset inPreference) {
        Helper.getHibernateSession().clear();
        this.ruleset = inPreference;
    }

    public void setRulesetId(int id) {
        this.rulesetId = id;
    }

    public int getRulesetId() {
        return this.rulesetId;
    }
}
