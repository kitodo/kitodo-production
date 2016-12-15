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

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.Helper;
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

import java.util.ArrayList;
import java.util.List;

public class GoobiProcessDAO {

    private static final Logger myLogger = Logger.getLogger(GoobiProcessDAO.class);

    public static GoobiProcess getProcessByPPN(IdentifierPPN PPN) {
        Session session;
        GoobiProcess result = null;

        session = Helper.getHibernateSession();

        try {

            Criteria criteria = session
                    .createCriteria(Prozess.class)
                    .createAlias("vorlagen", "v")
                    .createAlias("vorlagen.eigenschaften", "ve")
                    .createAlias("werkstuecke", "w")
                    .createAlias("werkstuecke.eigenschaften", "we")
                    .add(Restrictions.or(Restrictions.eq("we.titel", "PPN digital a-Satz"), Restrictions.eq("we.titel", "PPN digital f-Satz")))
                    .add(Restrictions.eq("ve.titel", "Titel"))
                    .add(Restrictions.eq("we.wert", PPN.toString()))
                    .addOrder(Order.asc("we.wert"))
                    .setProjection(Projections.projectionList()
                            .add(Projections.property("we.wert"), "identifier")
                            .add(Projections.property("ve.wert"), "title")
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
                    .createCriteria(Prozess.class)
                    .createAlias("vorlagen", "v")
                    .createAlias("vorlagen.eigenschaften", "ve")
                    .createAlias("werkstuecke", "w")
                    .createAlias("werkstuecke.eigenschaften", "we")
                    .add(Restrictions.or(Restrictions.eq("we.titel", "PPN digital a-Satz"), Restrictions.eq("we.titel", "PPN digital f-Satz")))
                    .add(Restrictions.eq("ve.titel", "Titel"))
                    .addOrder(Order.asc("we.wert"))
                    .setProjection(Projections.projectionList()
                            .add(Projections.property("we.wert"), "identifier")
                            .add(Projections.property("ve.wert"), "title")
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
                    .createCriteria(Schritt.class)
                    .createAlias("prozess", "p")
                    .createAlias("prozess.werkstuecke", "w")
                    .createAlias("prozess.werkstuecke.eigenschaften", "we")
                    .add(Restrictions.or(Restrictions.eq("we.titel", "PPN digital a-Satz"), Restrictions.eq("we.titel", "PPN digital f-Satz")))
                    .add(Restrictions.eq("we.wert", PPN.toString()))
                    .addOrder(Order.asc("reihenfolge"))
                    .setProjection(Projections.projectionList()
                            .add(Projections.property("reihenfolge"), "sequence")
                            .add(Projections.property("bearbeitungsstatus"), "state")
                            .add(Projections.property("titel"), "title")
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
