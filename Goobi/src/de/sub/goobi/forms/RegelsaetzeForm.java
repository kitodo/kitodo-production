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

import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.RegelsatzDAO;
import de.sub.goobi.persistence.apache.ProcessManager;

public class RegelsaetzeForm extends BasisForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Regelsatz myRegelsatz = new Regelsatz();
    private RegelsatzDAO dao = new RegelsatzDAO();
    private static final Logger logger = Logger.getLogger(RegelsaetzeForm.class);

    public String Neu() {
        this.myRegelsatz = new Regelsatz();
        return "RegelsaetzeBearbeiten";
    }

    public String Speichern() {
        try {
            if (hasValidRulesetFilePath(myRegelsatz, ConfigMain.getParameter("RegelsaetzeVerzeichnis"))) {
                dao.save(myRegelsatz);
                return "RegelsaetzeAlle";
            } else {
                Helper.setFehlerMeldung("RulesetNotFound");
                return "";
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
            logger.error(e);
            return "";
        }
    }

    private boolean hasValidRulesetFilePath(Regelsatz r, String pathToRulesets) {
        File rulesetFile = new File(pathToRulesets + r.getDatei());
        return rulesetFile.exists();
    }

    public String Loeschen() {
        try {
            if (hasAssignedProcesses(myRegelsatz)) {
                Helper.setFehlerMeldung("RulesetInUse");
                return "";
            } else {
                dao.remove(myRegelsatz);
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
            return "";
        }
        return "RegelsaetzeAlle";
    }

    private boolean hasAssignedProcesses(Regelsatz r) {
        Integer number = ProcessManager.getNumberOfProcessesWithRuleset(r.getId());
        if (number != null && number > 0) {
            return true;
        }
        return false;
    }

    public String FilterKein() {
        try {
            Session session = Helper.getHibernateSession();
            session.clear();
            Criteria crit = session.createCriteria(Regelsatz.class);
            crit.addOrder(Order.asc("titel"));
            this.page = new Page(crit, 0);
        } catch (HibernateException he) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
            return "";
        }
        return "RegelsaetzeAlle";
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

    /*
     * Getter und Setter
     */

    public Regelsatz getMyRegelsatz() {
        return this.myRegelsatz;
    }

    public void setMyRegelsatz(Regelsatz inPreference) {
        Helper.getHibernateSession().clear();
        this.myRegelsatz = inPreference;
    }
}
