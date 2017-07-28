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

import java.io.File;
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
import org.kitodo.services.ServiceManager;

@Named("RegelsaetzeForm")
@SessionScoped
public class RegelsaetzeForm extends BasisForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Ruleset myRegelsatz = new Ruleset();
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(RegelsaetzeForm.class);
    private int rulesetId;

    public String Neu() {
        this.myRegelsatz = new Ruleset();
        this.rulesetId = 0;
        return "/newpages/RegelsaetzeBearbeiten?faces-redirect=true";
    }

    /**
     * Save.
     *
     * @return page or empty String
     */
    public String Speichern() {
        try {
            if (hasValidRulesetFilePath(myRegelsatz, ConfigCore.getParameter("RegelsaetzeVerzeichnis"))) {
                serviceManager.getRulesetService().save(myRegelsatz);
                return "/newpages/RegelsaetzeAlle";
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

    private boolean hasValidRulesetFilePath(Ruleset r, String pathToRulesets) {
        File rulesetFile = new File(pathToRulesets + r.getFile());
        return rulesetFile.exists();
    }

    /**
     * Remove.
     *
     * @return page or empty String
     */
    public String Loeschen() {
        try {
            if (hasAssignedProcesses(myRegelsatz)) {
                Helper.setFehlerMeldung("RulesetInUse");
                return null;
            } else {
                serviceManager.getRulesetService().remove(myRegelsatz);
            }
        } catch (DataException e) {
            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
            return null;
        }
        return "/newpages/RegelsaetzeAlle";
    }

    private boolean hasAssignedProcesses(Ruleset r) throws DataException {
        Integer number = serviceManager.getProcessService().findByRuleset(r).size();
        return number != null && number > 0;
    }

    /**
     * No filtering.
     *
     * @return page or empty String
     */
    public String filterKein() {
        List<Ruleset> rulesets = serviceManager.getRulesetService().findAll();
        this.page = new Page(0, rulesets);
        return "/newpages/RegelsaetzeAlle";
    }

    /**
     * This method initializes the ruleset list without applying any filters
     * whenever the bean is constructed.
     */
    @PostConstruct
    public void initializeRulesetList() {
        filterKein();
    }

    public String FilterKeinMitZurueck() {
        filterKein();
        return this.zurueck;
    }

    /**
     * Method being used as viewAction for ruleset edit form. If 'rulesetId' is
     * '0', the form for creating a new ruleset will be displayed.
     */
    public void loadRuleset() {
        try {
            if (!Objects.equals(this.rulesetId, 0)) {
                setMyRegelsatz(this.serviceManager.getRulesetService().find(this.rulesetId));
            } else {
                setMyRegelsatz(null);
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error retrieving ruleset with ID '" + this.rulesetId + "'; ", e.getMessage());
        }
    }

    /*
     * Getter und Setter
     */

    public Ruleset getMyRegelsatz() {
        return this.myRegelsatz;
    }

    public void setMyRegelsatz(Ruleset inPreference) {
        Helper.getHibernateSession().clear();
        this.myRegelsatz = inPreference;
    }

    public void setRulesetId(int id) {
        this.rulesetId = id;
    }

    public int getRulesetId() {
        return this.rulesetId;
    }
}
