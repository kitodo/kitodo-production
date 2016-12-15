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

import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.helper.exceptions.DAOException;

public class BenutzergruppenDAO extends BaseDAO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4987176626562271217L;

	public Benutzergruppe save(Benutzergruppe t) throws DAOException {
		storeObj(t);
		return (Benutzergruppe) retrieveObj(Benutzergruppe.class, t.getId());
	}

	public Benutzergruppe get(Integer id) throws DAOException {
		Benutzergruppe rueckgabe = (Benutzergruppe) retrieveObj(Benutzergruppe.class, id);
		if (rueckgabe == null) {
			throw new DAOException("Object can not be found in database");
		}
		return rueckgabe;
	}

	public void remove(Benutzergruppe t) throws DAOException {
		if (t.getId() != null) {
			removeObj(t);
		}
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Benutzergruppe.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Benutzergruppe> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
}
