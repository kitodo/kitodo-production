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

package de.sub.goobi.forms;

import java.io.Serializable;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.persistence.BenutzerDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;

public class BasisForm implements Serializable {
	private static final Logger logger = Logger
	.getLogger(BasisForm.class);
	private static final long serialVersionUID = 2950419497162710096L;
	protected Page page;
	protected String zurueck = "";
	protected String filter = "";
	protected Benutzer user;

	protected String sortierung = "prozessAsc";

	public Page getPage() {
		return page;
	}

	public String getZurueck() {
		return zurueck;
	}

	public void setZurueck(String zurueck) {
		this.zurueck = zurueck;
	}
	
	public Benutzer getUser() {
		if(user==null) {
			LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
			user = login.getMyBenutzer();
		}
		return user;
	}
	
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getSortierung() {
		return sortierung;
	}

	public void setSortierung(String sortierung) {
		this.sortierung = sortierung;
	}

	public void addFilterToUser(){
		if (filter==null || filter.length()==0){
			return;
		}
		user.addFilter(filter);
		try {
			new BenutzerDAO().save(user);
		} catch (DAOException e) {
			logger.error(e);
		}
	}
	
	public void removeFilterFromUser(){
		if (filter==null || filter.length()==0){
			return;
		}
		user.removeFilter(filter);
		try {
			new BenutzerDAO().save(user);
		} catch (DAOException e) {
			logger.error(e);
		}		
	}
}
