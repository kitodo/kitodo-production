package de.sub.goobi.Persistence;

import de.sub.goobi.helper.exceptions.DAOException;

public class SimpleDAO extends BaseDAO {

	private static final long serialVersionUID = 599953115583442026L;

	public void save(Object t) throws DAOException {
		storeObj(t);
	}

	public void remove(Object t) throws DAOException {
		removeObj(t);
	}

	public void refresh(Object t) {
		refresh(t);
	}
}
