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

package de.sub.goobi.persistence;

import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

public class RegelsatzDAO extends BaseDAO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1913256950316879121L;

	public Regelsatz save(Regelsatz t) throws DAOException {
		storeObj(t);
		return (Regelsatz) retrieveObj(Regelsatz.class, t.getId());
	}

	public Regelsatz get(Integer id) throws DAOException {
		Regelsatz rueckgabe = (Regelsatz) retrieveObj(Regelsatz.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	public void remove(Regelsatz t) throws DAOException {
		if (t.getId() != null)
			removeObj(t);
	}

	public boolean hasAssignedProcesses(Regelsatz r) {
		Session newSession = Helper.getHibernateSession().getSessionFactory().openSession();
		Boolean result = false;
		try {
			Query q = newSession.createQuery("select count(*)>0 from Prozess as p where p.regelsatz = :regelsatz");
			q.setEntity("regelsatz", r);
			result = (Boolean) q.uniqueResult();
		} finally {
			if (newSession.isOpen()) {
				newSession.close();
			}
		}
		return result;
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Regelsatz.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Regelsatz> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
}
