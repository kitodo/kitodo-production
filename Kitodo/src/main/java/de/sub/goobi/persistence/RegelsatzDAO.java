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

import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.helper.exceptions.DAOException;

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

	/**
	 * @param id add description
	 * @return add description
	 * @throws DAOException add description
	 */
	public Regelsatz get(Integer id) throws DAOException {
		Regelsatz rueckgabe = (Regelsatz) retrieveObj(Regelsatz.class, id);
		if (rueckgabe == null) {
			throw new DAOException("Object can not be found in database");
		}
		return rueckgabe;
	}

	/**
	 * @param t add description
	 * @throws DAOException add description
	 */
	public void remove(Regelsatz t) throws DAOException {
		if (t.getId() != null) {
			removeObj(t);
		}
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
