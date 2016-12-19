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

import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.exceptions.DAOException;

public class SchrittDAO extends BaseDAO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2368830124391080142L;

	public Schritt save(Schritt t) throws DAOException {
		storeObj(t);
		return (Schritt) retrieveObj(Schritt.class, t.getId());
	}

	public Schritt get(Integer id) throws DAOException {
		Schritt rueckgabe = (Schritt) retrieveObj(Schritt.class, id);
		if (rueckgabe == null) {
			throw new DAOException("Object can not be found in database");
		}
		return rueckgabe;
	}

	public void remove(Schritt t) throws DAOException {
		if (t.getId() != null) {
			removeObj(t);
		}
	}

	public void remove(Integer id) throws DAOException {
		@SuppressWarnings("unused")
		Schritt t = (Schritt) retrieveObj(Schritt.class, id);
		removeObj(Schritt.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Schritt> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}

	public void refresh(Schritt t) {
		Object o = t;
		refresh(o);
	}
	
	public void update(Schritt t) {
		Object o = t;
		updateObj(o);
	}
	
	public Schritt load(int id) throws DAOException {
		return (Schritt) loadObj(Schritt.class, id);
	}
}
