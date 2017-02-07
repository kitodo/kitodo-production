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

import de.sub.goobi.beans.Projekt;
import de.sub.goobi.helper.exceptions.DAOException;

public class ProjektDAO extends BaseDAO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9050627256118458325L;

	public Projekt save(Projekt t) throws DAOException {
		storeObj(t);
		return (Projekt) retrieveObj(Projekt.class, t.getId());
	}

	public Projekt get(Integer id) throws DAOException {
		Projekt rueckgabe = (Projekt) retrieveObj(Projekt.class, id);
		if (rueckgabe == null) {
			throw new DAOException("Object cannot be found in database");
		}
		return rueckgabe;
	}

	public void remove(Projekt t) throws DAOException {
		if (t.getId() != null) {
			removeObj(t);
		}
	}

	public void remove(Integer id) throws DAOException {
		if (id != null) {
			removeObj(Projekt.class, id);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Projekt> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
}
