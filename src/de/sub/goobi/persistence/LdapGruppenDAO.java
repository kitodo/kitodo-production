/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.persistence;

import java.util.List;

import de.sub.goobi.beans.LdapGruppe;
import de.sub.goobi.helper.exceptions.DAOException;
import dubious.sub.goobi.persistence.BaseDAO;

@SuppressWarnings("serial")
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
