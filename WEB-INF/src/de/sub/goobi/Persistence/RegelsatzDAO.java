package de.sub.goobi.Persistence;

import java.util.List;

import de.sub.goobi.Beans.Regelsatz;
import de.sub.goobi.helper.exceptions.DAOException;

public class RegelsatzDAO extends BaseDAO {

	public Regelsatz save(Regelsatz t) throws DAOException {
		storeObj(t);
		return (Regelsatz) retrieveObj(Regelsatz.class, t.getId());
	}

	public Regelsatz get(Integer id) throws DAOException {
		Regelsatz rueckgabe = (Regelsatz) retrieveObj(Regelsatz.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	public void remove(Regelsatz t) throws DAOException {
		if (t.getId() != null)
			removeObj(t);
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Regelsatz.class, id);
	}

	public List<Regelsatz> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
}
