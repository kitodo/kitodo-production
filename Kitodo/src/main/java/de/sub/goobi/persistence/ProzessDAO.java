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

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.exceptions.DAOException;

import java.util.ArrayList;
import java.util.List;

public class ProzessDAO extends BaseDAO {

	private static final long serialVersionUID = 3538712266212954394L;

	/**
	 * @param t add description
	 * @return add description
	 * @throws DAOException add description
	 */
	public Prozess save(Prozess t) throws DAOException {
		t.setSortHelperStatus(t.getFortschritt());
		storeObj(t);
		return (Prozess) retrieveObj(Prozess.class, t.getId());
	}

	/**
	 * @param list add description
	 * @throws DAOException add description
	 */
	public void saveList(List<Prozess> list) throws DAOException {
		List<Object> l = new ArrayList<Object>();
		l.addAll(list);
		storeList(l);
	}

	/**
	 * @param id add description
	 * @return add description
	 * @throws DAOException add description
	 */
	public Prozess get(Integer id) throws DAOException {
		Prozess rueckgabe = (Prozess) retrieveObj(Prozess.class, id);
		if (rueckgabe == null) {
			throw new DAOException("Object can not be found in database");
		}
		return rueckgabe;
	}

	/**
	 * @param t add description
	 * @throws DAOException add description
	 */
	public void remove(Prozess t) throws DAOException {
		if (t.getId() != null) {
			removeObj(t);
		}
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Prozess.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Prozess> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}

	public void refresh(Prozess t) {
		Object o = t;
		refresh(o);
	}

	public void update(Prozess t) {
		Object o = t;
		updateObj(o);
	}

	public Prozess load(int id) throws DAOException {
		return (Prozess) loadObj(Prozess.class, id);
	}

}
