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
		if (rueckgabe == null) {
			throw new DAOException("Object cannot be found in database");
		}
		return rueckgabe;
	}

	/**
	 * 
	 * The function remove() removes a user from the environment. Since the user ID may still be referenced somewhere, the user account is invalidated
	 * instead.
	 * 
	 * @param user
	 *            User to be removed
	 * @throws DAOException
	 *             An exception that can be thrown from the underlying save() procedure upon database failure.
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
	 * @return List<Benutzer>
	 * @throws DAOException
	 */
	@SuppressWarnings("unchecked")
	public List<Benutzer> search(String query, String namedParameter, String parameter) throws DAOException {
		return retrieveObjs(query, namedParameter, parameter);
	}
}
