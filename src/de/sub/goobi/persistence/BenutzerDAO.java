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

import java.util.List;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.helper.exceptions.DAOException;

public class BenutzerDAO extends BaseDAO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 834210840673022251L;

	public Benutzer save(Benutzer t) throws DAOException {
		storeObj(t);
		return (Benutzer) retrieveObj(Benutzer.class, t.getId());
	}

	public Benutzer get(Integer id) throws DAOException {
		Benutzer rueckgabe = (Benutzer) retrieveObj(Benutzer.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	/**
	 * The function remove() removes a user from the environment. Since
	 * the user ID may still be referenced somewhere, the user account is
	 * invalidated instead.
	 * 
	 * @param user
	 *            User to be removed
	 * @throws DAOException
	 *             An exception that can be thrown from the underlying save()
	 *             procedure upon database failure.
	 */
	public void remove(Benutzer user) throws DAOException {
		user.selfDestruct();
		save(user);
	}

	@SuppressWarnings(value = "unchecked")
	public List<Benutzer> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}

	@SuppressWarnings(value = "unchecked")
	public List<Benutzer> search(String query, String parameter) throws DAOException {
   		return retrieveObjs(query, parameter);
	}

}
