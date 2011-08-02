package de.sub.goobi.Forms;
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
import java.io.Serializable;

import org.apache.log4j.Logger;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Persistence.BenutzerDAO;
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
		return this.page;
	}

	public String getZurueck() {
		return this.zurueck;
	}

	public void setZurueck(String zurueck) {
		this.zurueck = zurueck;
	}
	
	public Benutzer getUser() {
		if(this.user==null) {
			LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
			this.user = login.getMyBenutzer();
		}
		return this.user;
	}
	
	public String getFilter() {
		return this.filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getSortierung() {
		return this.sortierung;
	}

	public void setSortierung(String sortierung) {
		this.sortierung = sortierung;
	}

	public void addFilterToUser(){
		if (this.filter==null || this.filter.length()==0){
			return;
		}
		this.user.addFilter(this.filter);
		try {
			new BenutzerDAO().save(this.user);
		} catch (DAOException e) {
			logger.error(e);
		}
	}
	
	public void removeFilterFromUser(){
		if (this.filter==null || this.filter.length()==0){
			return;
		}
		this.user.removeFilter(this.filter);
		try {
			new BenutzerDAO().save(this.user);
		} catch (DAOException e) {
			logger.error(e);
		}		
	}
}
