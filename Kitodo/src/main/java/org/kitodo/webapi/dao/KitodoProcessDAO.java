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

package org.kitodo.webapi.dao;

import de.sub.kitodo.helper.Helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.webapi.beans.IdentifierPPN;
import org.kitodo.webapi.beans.KitodoProcess;
import org.kitodo.webapi.beans.KitodoProcessStep;

public class KitodoProcessDAO {

    private static final Logger myLogger = Logger.getLogger(KitodoProcessDAO.class);

    public static KitodoProcess getProcessByPPN(IdentifierPPN PPN) {
        Session session;
        KitodoProcess result = null;

        session = Helper.getHibernateSession();

        try {

            Criteria criteria = session.createCriteria(Process.class).createAlias("templates", "v")
                    .createAlias("templates.properties", "ve").createAlias("workpieces", "w")
                    .createAlias("workpieces.properties", "we")
                    .add(Restrictions.or(Restrictions.eq("we.title", "PPN digital a-Satz"),
                            Restrictions.eq("we.title", "PPN digital f-Satz")))
                    .add(Restrictions.eq("ve.title", "Titel")).add(Restrictions.eq("we.value", PPN.toString()))
                    .addOrder(Order.asc("we.value"))
                    .setProjection(Projections.projectionList().add(Projections.property("we.value"), "identifier")
                            .add(Projections.property("ve.value"), "title"))
                    .setResultTransformer(Transformers.aliasToBean(KitodoProcess.class));

            result = (KitodoProcess) criteria.uniqueResult();

        } catch (HibernateException he) {
            myLogger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

    public static List<KitodoProcess> getAllProcesses() {
        Session session;
        List<KitodoProcess> result;

        result = new ArrayList<KitodoProcess>();
        session = Helper.getHibernateSession();

        try {

            Criteria criteria = session.createCriteria(Process.class).createAlias("templates", "v")
                    .createAlias("templates.properties", "ve").createAlias("workpieces", "w")
                    .createAlias("workpieces.properties", "we")
                    .add(Restrictions.or(Restrictions.eq("we.title", "PPN digital a-Satz"),
                            Restrictions.eq("we.title", "PPN digital f-Satz")))
                    .add(Restrictions.eq("ve.title", "Titel")).addOrder(Order.asc("we.value"))
                    .setProjection(Projections.projectionList().add(Projections.property("we.value"), "identifier")
                            .add(Projections.property("ve.value"), "title"))
                    .setResultTransformer(Transformers.aliasToBean(KitodoProcess.class));

            @SuppressWarnings(value = "unchecked")
            List<KitodoProcess> list = criteria.list();

            if ((list != null) && (list.size() > 0)) {
                result.addAll(list);
            }
        } catch (HibernateException he) {
            myLogger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

    public static List<KitodoProcessStep> getAllProcessSteps(IdentifierPPN PPN) {
        List<KitodoProcessStep> result;
        Session session;

        result = new ArrayList<KitodoProcessStep>();
        session = Helper.getHibernateSession();

        try {

            Criteria criteria = session.createCriteria(Task.class).createAlias("process", "p")
                    .createAlias("process.workpieces", "w").createAlias("process.workpieces.properties", "we")
                    .add(Restrictions.or(Restrictions.eq("we.title", "PPN digital a-Satz"),
                            Restrictions.eq("we.title", "PPN digital f-Satz")))
                    .add(Restrictions.eq("we.wert", PPN.toString())).addOrder(Order.asc("reihenfolge"))
                    .setProjection(Projections.projectionList().add(Projections.property("ordering"), "sequence")
                            .add(Projections.property("processingStatus"), "state").add(Projections.property("title"),
                                    "title"))
                    .setResultTransformer(Transformers.aliasToBean(KitodoProcessStep.class));

            @SuppressWarnings(value = "unchecked")
            List<KitodoProcessStep> list = criteria.list();

            if ((list != null) && (!list.isEmpty())) {
                result.addAll(list);
            }
        } catch (HibernateException he) {
            myLogger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

}
