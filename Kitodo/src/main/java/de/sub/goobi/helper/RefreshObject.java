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

package de.sub.goobi.helper;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.persistence.HibernateUtilOld;

public class RefreshObject {
    private static final Logger logger = Logger.getLogger(RefreshObject.class);

    /**
     * Refresh process.
     *
     * @param processID
     *            int
     */
    public static void refreshProcess(int processID) {
        if (logger.isDebugEnabled()) {
            logger.debug("refreshing process with id " + processID);
        }
        try {
            Session session = HibernateUtilOld.getSessionFactory().openSession();
            if (session != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("session is connected: " + session.isConnected());
                    logger.debug("session is open: " + session.isOpen());
                }
            } else {
                logger.debug("session is null");
            }
            if ((session == null) || (!session.isOpen()) || (!session.isConnected())) {
                logger.debug("found no open session, don't refresh the process");
                if (session != null) {
                    session.close();
                    logger.debug("closed session");
                }
                return;
            }

            logger.debug("created a new session");
            Process o = (Process) session.get(Process.class, Integer.valueOf(processID));
            logger.debug("loaded process");
            session.refresh(o);
            logger.debug("refreshed process");
            session.close();
            logger.debug("closed session");
        } catch (Throwable e) {
            logger.error("cannot refresh process with id " + processID);
        }
    }

    /**
     * Refresh process - GUI.
     *
     * @param processID
     *            int
     */
    public static void refreshProcessGUI(int processID) {
        if (logger.isDebugEnabled()) {
            logger.debug("refreshing process with id " + processID);
        }
        Session session = null;
        boolean needsClose = false;
        try {
            session = Helper.getHibernateSession();
            if (session == null || !session.isOpen() || !session.isConnected()) {
                logger.debug("session is closed, creating a new session");
                HibernateUtilOld.rebuildSessionFactory();
                session = HibernateUtilOld.getSessionFactory().openSession();
                needsClose = true;
            }
            Process o = (Process) session.get(Process.class, processID);
            logger.debug("loaded process");
            session.refresh(o);
            logger.debug("refreshed process");
            if (needsClose) {
                session.close();
                logger.debug("closed session");
            }
        } catch (Throwable e) {
            logger.error("cannot refresh process with id " + processID);
            if (needsClose) {
                session.close();
            }
        }
    }

    /**
     * Stop refresh.
     *
     * @param stepID
     *            task id
     */
    public static void refreshStep(int stepID) {
        try {

            Session session = HibernateUtilOld.getSessionFactory().openSession();
            Task o = (Task) session.get(Task.class, stepID);
            session.refresh(o);
            session.close();
        } catch (Exception e) {
            logger.error("cannot refresh step with id " + stepID);
        }

    }

}
