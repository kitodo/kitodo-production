package de.sub.goobi.Persistence;

import java.util.List;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.exceptions.DAOException;

public class ProzessDAO extends BaseDAO {

	public Prozess save(Prozess t) throws DAOException {
		t.setSortHelperStatus(t.getFortschritt());
		storeObj(t);
		return (Prozess) retrieveObj(Prozess.class, t.getId());
	}

	public Prozess get(Integer id) throws DAOException {
		Prozess rueckgabe = (Prozess) retrieveObj(Prozess.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	public void remove(Prozess t) throws DAOException {
		if (t.getId() != null)
			removeObj(t);
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Prozess.class, id);
	}

	public List<Prozess> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
	
	public void refresh(Prozess t) {
		Object o = (Object) t;
		refresh(o);
	}
}
