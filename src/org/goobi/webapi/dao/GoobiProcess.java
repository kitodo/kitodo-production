/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.webapi.dao;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.Helper;
import org.apache.log4j.Logger;
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

public class GoobiProcess {

    private static final Logger myLogger = Logger.getLogger(GoobiProcess.class);

    public static org.goobi.webapi.beans.GoobiProcess getProcessByPPN(IdentifierPPN PPN) {
        Session session;
        org.goobi.webapi.beans.GoobiProcess result = null;

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
                    .setResultTransformer(Transformers.aliasToBean(org.goobi.webapi.beans.GoobiProcess.class));

            result = (org.goobi.webapi.beans.GoobiProcess) criteria.uniqueResult();

        } catch (HibernateException he) {
            myLogger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

    public static List<org.goobi.webapi.beans.GoobiProcess> getAllProcesses() {
        Session session;
        List<org.goobi.webapi.beans.GoobiProcess> result;

        result = new ArrayList<org.goobi.webapi.beans.GoobiProcess>();
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
                    .setResultTransformer(Transformers.aliasToBean(org.goobi.webapi.beans.GoobiProcess.class));

            @SuppressWarnings(value = "unchecked")
            List<org.goobi.webapi.beans.GoobiProcess> list = (List<org.goobi.webapi.beans.GoobiProcess>) criteria.list();

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
            List<GoobiProcessStep> list = (List<GoobiProcessStep>) criteria.list();

            if ((list != null) && (!list.isEmpty())) {
                result.addAll(list);
            }
        } catch (HibernateException he) {
            myLogger.error("Catched Hibernate exception: " + he.getMessage());
        }

        return result;
    }

}
