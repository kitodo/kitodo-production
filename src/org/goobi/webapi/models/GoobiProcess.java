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

import de.sub.goobi.helper.Helper;

import org.apache.log4j.Logger;
import org.goobi.webapi.beans.GoobiProcessInformation;
import org.hibernate.*;

import java.util.HashMap;
import java.util.Map;

public class GoobiProcess {

	private static final Logger myLogger = Logger.getLogger(GoobiProcess.class);

	public static Map<String, GoobiProcessInformation> getAllProcesses()	{
		Session session;
		String sqlQuery;
		Map<String, GoobiProcessInformation> map;

		map = new HashMap<String, GoobiProcessInformation>();
		session = Helper.getHibernateSession();

		try {

			sqlQuery = "SELECT we.Wert AS ppn,ve.Wert AS title "
				+ " FROM werkstuecke w "
				+ " INNER JOIN werkstueckeeigenschaften we ON we.werkstueckeID = w.werkstueckeID "
				+ " INNER JOIN vorlagen v ON v.ProzesseID = w.ProzesseID "
				+ " INNER JOIN vorlageneigenschaften ve ON ve.VorlagenID = v.VorlagenID "
				+ " WHERE ( we.Titel='PPN digital a-Satz' OR we.Titel='PPN digital f-Satz' ) "
				+ " AND ve.Titel='Titel' "
				+ " ORDER BY ppn ";

			Query query = session
					.createSQLQuery(sqlQuery)
					.addScalar("ppn", Hibernate.TEXT)
					.addScalar("title", Hibernate.TEXT);

			for (Object aQuery : query.list()) {
				Object row[] = (Object[]) aQuery;
				String identifier = (String) row[0];
				String title = (String) row[1];
				map.put(identifier, new GoobiProcessInformation(identifier, title));
			}

			/*
			// works but produces a hell of a lot sql queries
			Criteria criteria = session
					.createCriteria(Prozess.class)
					.createAlias("vorlagen", "v")
					.createAlias("vorlagen.eigenschaften", "ve")
					.createAlias("werkstuecke", "w")
					.createAlias("werkstuecke.eigenschaften", "we")
					.add(Restrictions.or(Restrictions.eq("we.titel", "PPN digital a-Satz"), Restrictions.eq("we.titel", "PPN digital f-Satz")))
					.add(Restrictions.eq("ve.titel", "Titel"))
					.addOrder(Order.asc("we.wert"))
					;
			List queryResults = criteria.list();

			for (Object row : queryResults) {
				Prozess prozess = (Prozess) row;
				String identifier = null;
				String title = null;
				for (Vorlage v : prozess.getVorlagenList()) {
					for (Vorlageeigenschaft ve : v.getEigenschaftenList()) {
						if (ve.getTitel().equals("Titel")) {
							title = ve.getWert();
						}
					}
				}
				for (Werkstueck w : prozess.getWerkstueckeList()) {
					for (Werkstueckeigenschaft we : w.getEigenschaftenList()) {
						String titel = we.getTitel();
						if (titel.equals("PPN digital a-Satz") || titel.equals("PPN ditial f-Satz")) {
							identifier = we.getWert();
						}
					}
				}
				if (identifier != null && title != null) {
					map.put(identifier, new GoobiProcessInformation(identifier, title));
				}
			}
			*/

		} catch (HibernateException he) {
			myLogger.error("Catched Hibernate exception: " + he.getMessage());
		}

		return map;
	}

}
