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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.apache.ProcessManager;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.ServiceManager;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class RegelsaetzeForm extends BasisForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Ruleset myRegelsatz = new Ruleset();
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(RegelsaetzeForm.class);

    public String Neu() {
        this.myRegelsatz = new Ruleset();
        return "RegelsaetzeBearbeiten";
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
                return "RegelsaetzeAlle";
            } else {
                Helper.setFehlerMeldung("RulesetNotFound");
                return "";
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
            logger.error(e);
            return "";
        } catch (IOException e) {
            logger.error(e);
            return "";
        } catch (CustomResponseException e) {
            logger.error("ElasticSearch server incorrect response",e);
            return "";
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
                return "";
            } else {
                serviceManager.getRulesetService().remove(myRegelsatz);
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
            return "";
        } catch (IOException e) {
            logger.error(e);
            return "";
        } catch (CustomResponseException e) {
            logger.error("ElasticSearch server incorrect response",e);
            return "";
        }
        return "RegelsaetzeAlle";
    }

    private boolean hasAssignedProcesses(Ruleset r) {
        Integer number = ProcessManager.getNumberOfProcessesWithRuleset(r.getId());
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
            return "";
        }
        return "RegelsaetzeAlle";
    }

    public String FilterKeinMitZurueck() {
        filterKein();
        return this.zurueck;
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
}
