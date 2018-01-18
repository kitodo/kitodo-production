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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

@Named("StatistikForm")
@ApplicationScoped
public class StatistikForm {
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(StatistikForm.class);

    /**
     * The function getAmountUsers() counts the number of user accounts in
     * the kitodo.production environment. Since user accounts are not hard
     * deleted from the database when the delete button is pressed a where
     * clause is used in the SQL statement to exclude the deleted accounts from
     * the sum.
     *
     * @return the count of valid user accounts
     */

    public Long getAmountUsers() {
        try {
            return serviceManager.getUserService().count();
        } catch (DataException e) {
            logger.error("ElasticSearch problem: ", e);
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
            return null;
        }
    }

    /**
     * Get amount of user groups.
     *
     * @return amount of user groups
     */
    public Long getAmountUserGroups() {
        try {
            return serviceManager.getUserGroupService().count();
        } catch (DataException e) {
            logger.error("ElasticSearch problem: ", e);
            Helper.setMeldung(null, "fehlerBeimEinlesen", e.getMessage());
            return null;
        }
    }

    /**
     * Get amount of processes.
     *
     * @return amount of processes
     */
    public Long getAmountProcesses() {
        try {
            return serviceManager.getProcessService().count();
        } catch (DataException e) {
            logger.error("ElasticSearch problem: ", e);
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
            return null;
        }
    }

    /**
     * Get amount of tasks.
     *
     * @return amount of tasks
     */
    public Long getAmountTasks() {
        try {
            return serviceManager.getTaskService().count();
        } catch (DataException e) {
            logger.error("ElasticSearch problem: ", e);
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e);
            return null;
        }
    }

    /**
     * Get amount of templates.
     *
     * @return amount of templates
     */
    public Long getAmountTemplates() {
        try {
            return serviceManager.getProcessService().countTemplates();
        } catch (DataException e) {
            logger.error("ElasticSearch problem: ", e);
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e);
            return null;
        }
    }

    public int getAmountOfCurrentTasks() {
        return getAmountOfCurrentTasks(false, false);
    }

    public int getAmountOfCurrentOpenTasks() {
        return getAmountOfCurrentTasks(true, false);
    }

    public int getAmountOfCurrentInProcessingTasks() {
        return getAmountOfCurrentTasks(false, true);
    }

    private int getAmountOfCurrentTasks(boolean open, boolean inProcessing) {
        Long amount = 0L;
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");

        if (login == null) {
            return 0;
        } else {
            if (login.getMyBenutzer() == null) {
                return 0;
            }
        }

        try {
            amount = serviceManager.getTaskService().getAmountOfCurrentTasks(open, inProcessing, login.getMyBenutzer());
        } catch (DataException e) {
            logger.error("ElasticSearch problem: ", e);
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e);
        }
        return amount.intValue();
    }

    public boolean getShowStatistics() {
        return ConfigCore.getBooleanParameter("showStatisticsOnStartPage", true);
    }
}
