package de.sub.goobi.persistence;

import java.util.List;

import org.goobi.production.search.lucene.LuceneIndex;

import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.exceptions.DAOException;

public class SchrittDAO extends BaseDAO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2368830124391080142L;

	public Schritt save(Schritt t) throws DAOException {
		storeObj(t);
		LuceneIndex.updateProcess(t.getProzess());
		return (Schritt) retrieveObj(Schritt.class, t.getId());
	}

	public Schritt get(Integer id) throws DAOException {
		Schritt rueckgabe = (Schritt) retrieveObj(Schritt.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	public void remove(Schritt t) throws DAOException {
		if (t.getId() != null) {
			removeObj(t);
			LuceneIndex.updateProcess(t.getProzess());
		}
	}

	public void remove(Integer id) throws DAOException {
		Schritt t = (Schritt) retrieveObj(Schritt.class, id);
		removeObj(Schritt.class, id);
		LuceneIndex.updateProcess(t.getProzess());
	}

	@SuppressWarnings("unchecked")
	public List<Schritt> search(String query) throws DAOException {
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
