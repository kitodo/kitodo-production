package de.sub.goobi.Forms;

import java.io.Serializable;

import de.sub.goobi.helper.Page;

public class BasisForm implements Serializable {
	private static final long serialVersionUID = 2950419497162710096L;
	protected Page page;
	protected String zurueck = "";
	protected String filter = "";
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

}
