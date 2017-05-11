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
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.apache.ProcessManager;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.ServiceManager;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class DocketForm extends BasisForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Docket myDocket = new Docket();
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(DocketForm.class);

    public String Neu() {
        this.myDocket = new Docket();
        return "/newpages/DocketEdit";
    }

    /**
     * Save docket.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            if (hasValidRulesetFilePath(myDocket, ConfigCore.getParameter("xsltFolder"))) {
                this.serviceManager.getDocketService().save(myDocket);
                return "/newpages/DocketList";
            } else {
                Helper.setFehlerMeldung("DocketNotFound");
                return null;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
            logger.error(e);
            return null;
        } catch (IOException e) {
            Helper.setFehlerMeldung("errorElasticSearch", e.getMessage());
            logger.error(e);
            return null;
        } catch (CustomResponseException e) {
            Helper.setFehlerMeldung("ElasticSearch server response incorrect", e.getMessage());
            logger.error(e);
            return null;
        }
    }

    private boolean hasValidRulesetFilePath(Docket d, String pathToRulesets) {
        File rulesetFile = new File(pathToRulesets + d.getFile());
        return rulesetFile.exists();
    }

    /**
     * Delete docket.
     *
     * @return page or empty String
     */
    public String deleteDocket() {
        try {
            if (hasAssignedProcesses(myDocket)) {
                Helper.setFehlerMeldung("DocketInUse");
                return null;
            } else {
                this.serviceManager.getDocketService().remove(this.myDocket);
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
            return null;
        } catch (IOException e) {
            Helper.setFehlerMeldung("errorElasticSearch", e.getMessage());
            return null;
        } catch (CustomResponseException e) {
            Helper.setFehlerMeldung("ElasticSearch server response incorrect", e.getMessage());
            logger.error(e);
            return null;
        }
        return "/newpages/DocketList";
    }

    private boolean hasAssignedProcesses(Docket d) {
        Integer number = ProcessManager.getNumberOfProcessesWithDocket(d.getId());
        if (number != null && number > 0) {
            return true;
        }
        return false;
    }

    /**
     * No filter.
     *
     * @return page or empty String
     */
    public String filterKein() {
        try {
            // HibernateUtil.clearSession();
            Session session = Helper.getHibernateSession();
            // session.flush();
            session.clear();
            Criteria crit = session.createCriteria(Docket.class);
            crit.addOrder(Order.asc("title"));
            this.page = new Page(crit, 0);
        } catch (HibernateException he) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
            return null;
        }
        return "/newpages/DocketList";
    }

    public String filterKeinMitZurueck() {
        filterKein();
        return this.zurueck;
    }

    /*
     * Getter und Setter
     */

    public Docket getMyDocket() {
        return this.myDocket;
    }

    public void setMyDocket(Docket docket) {
        Helper.getHibernateSession().clear();
        this.myDocket = docket;
    }
}
