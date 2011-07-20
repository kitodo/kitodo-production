package de.sub.goobi.Persistence;

import java.util.List;

import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.helper.exceptions.DAOException;

public class SchrittDAO extends BaseDAO {

	public Schritt save(Schritt t) throws DAOException {
		storeObj(t);
		return (Schritt) retrieveObj(Schritt.class, t.getId());
	}

	public Schritt get(Integer id) throws DAOException {
		Schritt rueckgabe = (Schritt) retrieveObj(Schritt.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	public void remove(Schritt t) throws DAOException {
		if (t.getId() != null)
			removeObj(t);
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Schritt.class, id);
	}

	public List search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
	
	public void refresh(Schritt t) {
		Object o = (Object) t;
		refresh(o);
	}
}
