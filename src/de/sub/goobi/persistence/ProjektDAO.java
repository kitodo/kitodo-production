package de.sub.goobi.persistence;

import java.util.List;

import de.sub.goobi.beans.Projekt;
import de.sub.goobi.helper.exceptions.DAOException;

public class ProjektDAO extends BaseDAO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9050627256118458325L;

	public Projekt save(Projekt t) throws DAOException {
		storeObj(t);
//		for (Prozess p : t.getProzesse()) {
//			LuceneIndex.updateProcess(p);
//		}
		return (Projekt) retrieveObj(Projekt.class, t.getId());
	}

	public Projekt get(Integer id) throws DAOException {
		Projekt rueckgabe = (Projekt) retrieveObj(Projekt.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	public void remove(Projekt t) throws DAOException {
		if (t.getId() != null) {
//			for (Prozess p : t.getProzesse()) {
//				LuceneIndex.deleteProcess(p);
//			}
			removeObj(t);
		}
	}

	public void remove(Integer id) throws DAOException {
		if (id != null) {
//			Projekt t = (Projekt) retrieveObj(Projekt.class, id);
//			for (Prozess p : t.getProzesse()) {
//				LuceneIndex.deleteProcess(p);
//			}
			removeObj(Projekt.class, id);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Projekt> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
}
