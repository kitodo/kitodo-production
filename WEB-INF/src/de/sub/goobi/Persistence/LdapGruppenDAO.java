package de.sub.goobi.Persistence;

import java.util.List;

import de.sub.goobi.Beans.LdapGruppe;
import de.sub.goobi.helper.exceptions.DAOException;

public class LdapGruppenDAO extends BaseDAO {

	public LdapGruppe save(LdapGruppe t) throws DAOException {
		storeObj(t);
		return (LdapGruppe) retrieveObj(LdapGruppe.class, t.getId());
	}

	public LdapGruppe get(Integer id) throws DAOException {
		LdapGruppe rueckgabe = (LdapGruppe) retrieveObj(LdapGruppe.class, id);
		if (rueckgabe == null)
			throw new DAOException("Object can not be found in database");
		return rueckgabe;
	}

	public void remove(LdapGruppe t) throws DAOException {
		if (t.getId() != null)
			removeObj(t);
	}

	public void remove(Integer id) throws DAOException {
		removeObj(LdapGruppe.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<LdapGruppe> search(String query) throws DAOException {
		return (List<LdapGruppe>) retrieveObjs(query);
	}

}
