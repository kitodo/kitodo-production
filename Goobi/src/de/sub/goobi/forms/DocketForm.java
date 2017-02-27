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

import de.sub.goobi.beans.Docket;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.DocketDAO;
import de.sub.goobi.persistence.apache.ProcessManager;

public class DocketForm extends BasisForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Docket myDocket = new Docket();
    private DocketDAO dao = new DocketDAO();
    private static final Logger logger = Logger.getLogger(DocketForm.class);

    public String Neu() {
        this.myDocket = new Docket();
        return "DocketEdit";
    }

    public String Speichern() {
        try {
            if (hasValidRulesetFilePath(myDocket, ConfigMain.getParameter("xsltFolder"))) {
                this.dao.save(myDocket);
                return "DocketList";
            } else {
                Helper.setFehlerMeldung("DocketNotFound");
                return "";
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
            logger.error(e);
            return "";
        }
    }

    private boolean hasValidRulesetFilePath(Docket d, String pathToRulesets) {
        File rulesetFile = new File(pathToRulesets + d.getFile());
        return rulesetFile.exists();
    }

    public String Loeschen() {
        try {
            if (hasAssignedProcesses(myDocket)) {
                Helper.setFehlerMeldung("DocketInUse");
                return "";
            } else {
                this.dao.remove(this.myDocket);
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
            return "";
        }
        return "DocketList";
    }

    private boolean hasAssignedProcesses(Docket d) {
        Integer number = ProcessManager.getNumberOfProcessesWithDocket(d.getId());
        if (number != null && number > 0) {
            return true;
        }
        return false;
    }

    public String FilterKein() {
        try {
            // HibernateUtil.clearSession();
            Session session = Helper.getHibernateSession();
            // session.flush();
            session.clear();
            Criteria crit = session.createCriteria(Docket.class);
            crit.addOrder(Order.asc("name"));
            this.page = new Page(crit, 0);
        } catch (HibernateException he) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
            return "";
        }
        return "DocketList";
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

    /*
     * ##################################################### ##################################################### ## ## Getter und Setter ##
     * ##################################################### ####################################################
     */

    public Docket getMyDocket() {
        return this.myDocket;
    }

    public void setMyDocket(Docket docket) {
        Helper.getHibernateSession().clear();
        this.myDocket = docket;
    }
}
