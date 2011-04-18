package de.sub.goobi.Forms;

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
