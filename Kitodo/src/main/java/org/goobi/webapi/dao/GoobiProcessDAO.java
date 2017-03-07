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

import org.apache.log4j.Logger;

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

    private static final Logger myLogger = Logger.getLogger(GoobiProcessDAO.class);

    public static GoobiProcess getProcessByPPN(IdentifierPPN PPN) {
        Session session;
        GoobiProcess result = null;

        session = Helper.getHibernateSession();

        try {

            Criteria criteria = session
                    .createCriteria(Process.class)
                    .createAlias("templates", "v")
                    .createAlias("templates.properties", "ve")
                    .createAlias("workpieces", "w")
                    .createAlias("workpieces.properties", "we")
                    .add(Restrictions.or(Restrictions.eq("we.title", "PPN digital a-Satz"), Restrictions.eq("we.title", "PPN digital f-Satz")))
                    .add(Restrictions.eq("ve.title", "Titel"))
                    .add(Restrictions.eq("we.value", PPN.toString()))
                    .addOrder(Order.asc("we.value"))
                    .setProjection(Projections.projectionList()
                            .add(Projections.property("we.value"), "identifier")
                            .add(Projections.property("ve.value"), "title")
                    )
                    .setResultTransformer(Transformers.aliasToBean(GoobiProcess.class));

            result = (GoobiProcess) criteria.uniqueResult();

        } catch (HibernateException he) {
            myLogger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

    public static List<GoobiProcess> getAllProcesses() {
        Session session;
        List<GoobiProcess> result;

        result = new ArrayList<GoobiProcess>();
        session = Helper.getHibernateSession();

        try {

            Criteria criteria = session
                    .createCriteria(Process.class)
                    .createAlias("templates", "v")
                    .createAlias("templates.properties", "ve")
                    .createAlias("workpieces", "w")
                    .createAlias("workpieces.properties", "we")
                    .add(Restrictions.or(Restrictions.eq("we.title", "PPN digital a-Satz"), Restrictions.eq("we.title", "PPN digital f-Satz")))
                    .add(Restrictions.eq("ve.title", "Titel"))
                    .addOrder(Order.asc("we.value"))
                    .setProjection(Projections.projectionList()
                            .add(Projections.property("we.value"), "identifier")
                            .add(Projections.property("ve.value"), "title")
                    )
                    .setResultTransformer(Transformers.aliasToBean(GoobiProcess.class));

            @SuppressWarnings(value = "unchecked")
            List<GoobiProcess> list = criteria.list();

            if ((list != null) && (list.size() > 0)) {
                result.addAll(list);
            }
        } catch (HibernateException he) {
            myLogger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

    public static List<GoobiProcessStep> getAllProcessSteps(IdentifierPPN PPN) {
        List<GoobiProcessStep> result;
        Session session;

        result = new ArrayList<GoobiProcessStep>();
        session = Helper.getHibernateSession();

        try {

            Criteria criteria = session
                    .createCriteria(Task.class)
                    .createAlias("process", "p")
                    .createAlias("process.workpieces", "w")
                    .createAlias("process.workpieces.properties", "we")
                    .add(Restrictions.or(Restrictions.eq("we.title", "PPN digital a-Satz"), Restrictions.eq("we.title", "PPN digital f-Satz")))
                    .add(Restrictions.eq("we.wert", PPN.toString()))
                    .addOrder(Order.asc("reihenfolge"))
                    .setProjection(Projections.projectionList()
                            .add(Projections.property("ordering"), "sequence")
                            .add(Projections.property("processingStatus"), "state")
                            .add(Projections.property("title"), "title")
                    )
                    .setResultTransformer(Transformers.aliasToBean(GoobiProcessStep.class));

            @SuppressWarnings(value = "unchecked")
            List<GoobiProcessStep> list = criteria.list();

            if ((list != null) && (!list.isEmpty())) {
                result.addAll(list);
            }
        } catch (HibernateException he) {
            myLogger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

}
