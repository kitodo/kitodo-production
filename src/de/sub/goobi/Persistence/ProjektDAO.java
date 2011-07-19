package de.sub.goobi.Persistence;

import java.util.List;

import de.sub.goobi.Beans.Projekt;
import de.sub.goobi.helper.exceptions.DAOException;

public class ProjektDAO extends BaseDAO {

	public Projekt save(Projekt t) throws DAOException {
		storeObj(t);
		return (Projekt) retrieveObj(Projekt.class, t.getId());
	}

	public Projekt get(Integer id) throws DAOException {
		Projekt rueckgabe = (Projekt) retrieveObj(Projekt.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	public void remove(Projekt t) throws DAOException {
		if (t.getId() != null)
			removeObj(t);
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Projekt.class, id);
	}

	public List search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
}
