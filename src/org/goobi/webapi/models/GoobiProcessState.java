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

package org.goobi.webapi.models;

import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.Helper;

import org.apache.log4j.Logger;

import org.goobi.webapi.beans.GoobiProcessStep;
import org.goobi.webapi.validators.IdentifierPpn;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import java.util.ArrayList;
import java.util.List;

public class GoobiProcessState {

	private static final Logger myLogger = Logger.getLogger(GoobiProcessState.class);

	public static List<GoobiProcessStep> getProcessState(String ppnIdentifier) {
		List<GoobiProcessStep> result;
		Session session;

		result = new ArrayList<GoobiProcessStep>();
		session = Helper.getHibernateSession();

		if (!IdentifierPpn.isValid(ppnIdentifier)) {
			myLogger.warn("Invalid PPN " + ppnIdentifier + " given!");
			return result;
		}

		try {

			Criteria criteria = session
					.createCriteria(Schritt.class)
					.createAlias("prozess", "p")
					.createAlias("prozess.werkstuecke", "w")
					.createAlias("prozess.werkstuecke.eigenschaften", "we")
					.add(Restrictions.or(Restrictions.eq("we.titel", "PPN digital a-Satz"), Restrictions.eq("we.titel", "PPN digital f-Satz")))
					.add(Restrictions.eq("we.wert", ppnIdentifier))
					.addOrder(Order.asc("reihenfolge"))
					.setProjection(Projections.projectionList()
							.add(Projections.property("reihenfolge"), "sequence")
							.add(Projections.property("bearbeitungsstatus"), "state")
							.add(Projections.property("titel"), "title")
					)
					.setResultTransformer(Transformers.aliasToBean(GoobiProcessStep.class))
					;

			@SuppressWarnings(value = "unchecked")
			List<GoobiProcessStep> list = (List<GoobiProcessStep>) criteria.list();

			if ((list != null) && (list.size() > 0)) {
				result.addAll(list);
			}
		} catch (HibernateException he) {
			myLogger.error("Catched Hibernate exception: " + he.getMessage());
		}

		return result;
	}


}
