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
import java.util.List;

import de.sub.goobi.Beans.Projekt;
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
		if (rueckgabe == null) {
			throw new DAOException("Object can not be found in database");
		}
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
