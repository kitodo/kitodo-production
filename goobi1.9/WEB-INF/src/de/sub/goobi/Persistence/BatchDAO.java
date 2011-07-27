package de.sub.goobi.Persistence;

import java.util.List;

import de.sub.goobi.Beans.Batch;
import de.sub.goobi.helper.exceptions.DAOException;

public class BatchDAO extends BaseDAO {


	private static final long serialVersionUID = 3538712266212954394L;

	public Batch save(Batch t) throws DAOException {
//		t.setSortHelperStatus(t.getFortschritt());
		storeObj(t);
		return (Batch) retrieveObj(Batch.class, t.getId());
	}

	public Batch get(Integer id) throws DAOException {
		Batch rueckgabe = (Batch) retrieveObj(Batch.class, id);
		if (rueckgabe == null) {
			throw new DAOException("Object can not be found in database");
		}
		return rueckgabe;
	}

	public void remove(Batch t) throws DAOException {
		if (t.getId() != null) {
			removeObj(t);
		}
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Batch.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Batch> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
	
	public void refresh(Batch t) {
		Object o = t;
		refresh(o);
	}
}
