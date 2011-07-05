package de.sub.goobi.Persistence;

import java.util.List;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.helper.exceptions.DAOException;

public class BenutzerDAO extends BaseDAO {

	public Benutzer save(Benutzer t) throws DAOException {
		storeObj(t);
		return (Benutzer) retrieveObj(Benutzer.class, t.getId());
	}

	public Benutzer get(Integer id) throws DAOException {
		Benutzer rueckgabe = (Benutzer) retrieveObj(Benutzer.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	public void remove(Benutzer t) throws DAOException {
		if (t.getId() != null)
			removeObj(t);
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Benutzer.class, id);
	}

	public List search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
}
