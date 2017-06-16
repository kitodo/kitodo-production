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
import java.io.IOException;
import java.util.Objects;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.json.simple.parser.ParseException;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.ServiceManager;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class RegelsaetzeForm extends BasisForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Ruleset myRegelsatz = new Ruleset();
    private final ServiceManager serviceManager = new ServiceManager();
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
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
            logger.error(e);
            return null;
        } catch (IOException e) {
            logger.error(e);
            return null;
        } catch (CustomResponseException e) {
            logger.error("ElasticSearch server incorrect response", e);
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
    public String Loeschen() throws ParseException {
        try {
            if (hasAssignedProcesses(myRegelsatz)) {
                Helper.setFehlerMeldung("RulesetInUse");
                return null;
            } else {
                serviceManager.getRulesetService().remove(myRegelsatz);
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
            return null;
        } catch (IOException e) {
            logger.error(e);
            return null;
        } catch (CustomResponseException e) {
            logger.error("ElasticSearch server incorrect response", e);
            return null;
        }
        return "/newpages/RegelsaetzeAlle";
    }

    private boolean hasAssignedProcesses(Ruleset r) throws ParseException, CustomResponseException, IOException {
        Integer number = serviceManager.getProcessService().findByRuleset(r).size();
        if (number != null && number > 0) {
            return true;
        }
        return false;
    }

    /**
     * No filtering.
     *
     * @return page or empty String
     */
    public String filterKein() {
        try {
            Session session = Helper.getHibernateSession();
            session.clear();
            Criteria crit = session.createCriteria(Ruleset.class);
            crit.addOrder(Order.asc("title"));
            this.page = new Page(crit, 0);
        } catch (HibernateException he) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
            return null;
        }
        return "/newpages/RegelsaetzeAlle";
    }

    /**
     * This method initializes the ruleset list without applying any filters whenever the bean is constructed.
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
     * Method being used as viewAction for ruleset edit form.
     * If 'rulesetId' is '0', the form for creating a new ruleset will be displayed.
     */
    public void loadRuleset() {
        try {
            if(!Objects.equals(this.rulesetId, 0)) {
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

    public void setRulesetId(int id) { this.rulesetId = id; }

    public int getRulesetId() { return this.rulesetId; }
}
