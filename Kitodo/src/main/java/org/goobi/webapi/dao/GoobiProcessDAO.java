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

package org.goobi.webapi.dao;

import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.webapi.beans.GoobiProcess;
import org.goobi.webapi.beans.GoobiProcessStep;
import org.goobi.webapi.beans.IdentifierPPN;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;

public class GoobiProcessDAO {

    private static final Logger logger = LogManager.getLogger(GoobiProcessDAO.class);

    /**
     * Private constructor to hide the implicit public one.
     */
    private GoobiProcessDAO() {

    }

    /**
     * Get process by PPN.
     * 
     * @param ppn
     *            as IdentifierPPN
     * @return GoobiProcess
     */
    public static GoobiProcess getProcessByPPN(IdentifierPPN ppn) {
        Session session;
        GoobiProcess result = null;

        session = Helper.getHibernateSession();

        try {

            Criteria criteria = session.createCriteria(Process.class).createAlias("templates", "v")
                    .createAlias("templates.properties", "ve").createAlias("workpieces", "w")
                    .createAlias("workpieces.properties", "we")
                    .add(Restrictions.or(Restrictions.eq("we.title", "PPN digital a-Satz"),
                            Restrictions.eq("we.title", "PPN digital f-Satz")))
                    .add(Restrictions.eq("ve.title", "Titel")).add(Restrictions.eq("we.value", ppn.toString()))
                    .addOrder(Order.asc("we.value"))
                    .setProjection(Projections.projectionList().add(Projections.property("we.value"), "identifier")
                            .add(Projections.property("ve.value"), "title"))
                    .setResultTransformer(Transformers.aliasToBean(GoobiProcess.class));

            result = (GoobiProcess) criteria.uniqueResult();

        } catch (HibernateException he) {
            logger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

    /**
     * Get all processes.
     * 
     * @return List of GoobiProcess objects
     */
    public static List<GoobiProcess> getAllProcesses() {
        Session session = Helper.getHibernateSession();
        List<GoobiProcess> result = new ArrayList<>();

        try {
            Criteria criteria = session.createCriteria(Process.class).createAlias("templates", "v")
                    .createAlias("templates.properties", "ve").createAlias("workpieces", "w")
                    .createAlias("workpieces.properties", "we")
                    .add(Restrictions.or(Restrictions.eq("we.title", "PPN digital a-Satz"),
                            Restrictions.eq("we.title", "PPN digital f-Satz")))
                    .add(Restrictions.eq("ve.title", "Titel")).addOrder(Order.asc("we.value"))
                    .setProjection(Projections.projectionList().add(Projections.property("we.value"), "identifier")
                            .add(Projections.property("ve.value"), "title"))
                    .setResultTransformer(Transformers.aliasToBean(GoobiProcess.class));

            @SuppressWarnings(value = "unchecked")
            List<GoobiProcess> list = criteria.list();
            if (Objects.nonNull(list) && !list.isEmpty()) {
                result.addAll(list);
            }
        } catch (HibernateException he) {
            logger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

    /**
     * Get all process tasks.
     * 
     * @param ppn
     *            as IdentifierPPN
     * @return List of GoobiProcessStep objects
     */
    public static List<GoobiProcessStep> getAllProcessSteps(IdentifierPPN ppn) {
        List<GoobiProcessStep> result = new ArrayList<>();
        Session session = Helper.getHibernateSession();

        try {
            Criteria criteria = session.createCriteria(Task.class).createAlias("process", "p")
                    .createAlias("process.workpieces", "w").createAlias("process.workpieces.properties", "we")
                    .add(Restrictions.or(Restrictions.eq("we.title", "PPN digital a-Satz"),
                            Restrictions.eq("we.title", "PPN digital f-Satz")))
                    .add(Restrictions.eq("we.wert", ppn.toString())).addOrder(Order.asc("reihenfolge"))
                    .setProjection(Projections.projectionList().add(Projections.property("ordering"), "sequence")
                            .add(Projections.property("processingStatus"), "state").add(Projections.property("title"),
                                    "title"))
                    .setResultTransformer(Transformers.aliasToBean(GoobiProcessStep.class));

            @SuppressWarnings(value = "unchecked")
            List<GoobiProcessStep> list = criteria.list();

            if ((list != null) && (!list.isEmpty())) {
                result.addAll(list);
            }
        } catch (HibernateException he) {
            logger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

}
