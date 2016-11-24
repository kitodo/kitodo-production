package de.sub.goobi.persistence;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.helper.exceptions.DAOException;

import java.util.List;

public class BenutzerDAO extends BaseDAO {

	/**
	 *
	 */
	private static final long serialVersionUID = 834210840673022251L;

	public Benutzer save(Benutzer t) throws DAOException {
		storeObj(t);
		return (Benutzer) retrieveObj(Benutzer.class, t.getId());
	}

	/**
	 * @param id add description
	 * @return add description
	 * @throws DAOException add description
	 */
	public Benutzer get(Integer id) throws DAOException {
		Benutzer rueckgabe = (Benutzer) retrieveObj(Benutzer.class, id);
		if (rueckgabe == null) {
			throw new DAOException("Object can not be found in database");
		}
		return rueckgabe;
	}

	/**
	 * The function remove() removes a user from the environment. Since the user ID may still be referenced somewhere,
	 * the user account is invalidated instead.
	 *
	 * @param user User to be removed
	 * @throws DAOException An exception that can be thrown from the underlying save() procedure upon database failure.
	 */

	public void remove(Benutzer user) throws DAOException {
		user.selfDestruct();
		save(user);
	}

	@SuppressWarnings("unchecked")
	public List<Benutzer> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}

	public void refresh(Benutzer t) {
		Object o = t;
		refresh(o);
	}

	@SuppressWarnings("unchecked")
	public List<Benutzer> search(String query, String parameter) throws DAOException {
		return retrieveObjs(query, parameter);
	}

	/**
	 * Search for a list of users by a named parameter
	 *
	 * @param query Search query
	 * @param namedParameter Name of named parameter
	 * @param parameter Parameter value
	 * @return List	&lt;Benutzer&gt;
	 * @throws DAOException add description
	 */
	@SuppressWarnings("unchecked")
	public List<Benutzer> search(String query, String namedParameter, String parameter) throws DAOException {
		return retrieveObjs(query, namedParameter, parameter);
	}
}
