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

import de.sub.goobi.beans.Docket;
import de.sub.goobi.helper.exceptions.DAOException;

public class DocketDAO extends BaseDAO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1913256950316879121L;

	public Docket save(Docket t) throws DAOException {
		storeObj(t);
		return (Docket) retrieveObj(Docket.class, t.getId());
	}

	public Docket get(Integer id) throws DAOException {
		Docket rueckgabe = (Docket) retrieveObj(Docket.class, id);
		if (rueckgabe == null) {
			throw new DAOException("Object cannot be found in database");
		}
		return rueckgabe;
	}

	public void remove(Docket t) throws DAOException {
		if (t.getId() != null) {
			removeObj(t);
		}
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Docket.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Docket> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
}
