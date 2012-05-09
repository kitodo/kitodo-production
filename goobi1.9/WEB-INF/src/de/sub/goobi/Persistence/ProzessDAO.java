package de.sub.goobi.Persistence;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.util.ArrayList;
import java.util.List;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.helper.exceptions.DAOException;

public class ProzessDAO extends BaseDAO {


	private static final long serialVersionUID = 3538712266212954394L;

	public Prozess save(Prozess t) throws DAOException {
		t.setSortHelperStatus(t.getFortschritt());
		storeObj(t);
		return (Prozess) retrieveObj(Prozess.class, t.getId());
	}

	public void saveList(List<Prozess> list) throws DAOException {
		List<Object> l = new ArrayList<Object>();
		l.addAll(list);
		storeList(l);
	}
	
	public Prozess get(Integer id) throws DAOException {
		Prozess rueckgabe = (Prozess) retrieveObj(Prozess.class, id);
		if (rueckgabe == null) {
			throw new DAOException("Object can not be found in database");
		}
		return rueckgabe;
	}

	public void remove(Prozess t) throws DAOException {
		if (t.getId() != null) {
			removeObj(t);
		}
	}

	public void remove(Integer id) throws DAOException {
		removeObj(Prozess.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Prozess> search(String query) throws DAOException {
		return retrieveObjs(query);
	}

	public Long count(String query) throws DAOException {
		return retrieveAnzahl(query);
	}
	
	public void refresh(Prozess t) {
		Object o = t;
		refresh(o);
	}
	public void update(Prozess t) {
		Object o = t;
		updateObj(o);
	}
	
	public Prozess load(int id) throws DAOException {
		return (Prozess) loadObj(Prozess.class, id);
	}
	
}
