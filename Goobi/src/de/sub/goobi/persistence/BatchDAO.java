package de.sub.goobi.persistence;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;

public class BatchDAO extends BaseDAO {
	private static final long serialVersionUID = 1L;
	private final static BaseDAO parent = new BatchDAO();

	public static Batch read(Integer id) throws DAOException {
		return (Batch) parent.retrieveObj(Batch.class, id);
	}

	public static void deleteAll(Iterable<Integer> ids) throws DAOException {
		for (Integer id : ids)
			parent.removeObj(Batch.class, id);
	}

	public static void save(Batch a) throws DAOException {
		parent.storeObj(a);
	}

	public static Batch reattach(Batch batch) {
		Session session = Helper.getHibernateSession();
		session.refresh(batch);
		return batch;
	}

	@SuppressWarnings("unchecked")
	public static List<Batch> readAll() {
		Session session = Helper.getHibernateSession();
		Criteria criteria = session.createCriteria(Batch.class);
		return criteria.list();
	}

}
