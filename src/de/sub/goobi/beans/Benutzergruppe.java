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

package de.sub.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Usergroups owning different access rights, represented by integer values
 * 
 * 1: Administration - can do anything 2: Projectmanagement - may do a lot (but not user management, no user switch, no administrative form) 3: User
 * and process (basically like 4 but can be used for setting aditional boundaries later, if so desired) 4: User only: can see current steps
 * 
 * ================================================================
 */
public class Benutzergruppe implements Serializable {
	private static final long serialVersionUID = -5924845694417474352L;
	private Integer id;
	private String titel;
	private Integer berechtigung;
	private Set<Benutzer> benutzer;
	private Set<Schritt> schritte;
	private boolean panelAusgeklappt = false;

	public Benutzergruppe() {
		schritte = new HashSet<Schritt>();
		benutzer = new HashSet<Benutzer>();
	}

	/*
	 * ##################################################### ##################################################### ## ## Getter und Setter ##
	 * ##################################################### ####################################################
	 */

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBerechtigung() {
		if (berechtigung == null) {
			berechtigung = 4;
		} else if (berechtigung == 3) {
			berechtigung = 4;
		}
		return berechtigung;
	}

	public void setBerechtigung(int berechtigung) {
		this.berechtigung = berechtigung;
	}

	public String getBerechtigungAsString() {
		if (berechtigung == null) {
			berechtigung = 4;
		} else if (berechtigung == 3) {
			berechtigung = 4;
		}
		return String.valueOf(berechtigung.intValue());
	}

	public void setBerechtigungAsString(String berechtigung) {
		this.berechtigung = Integer.parseInt(berechtigung);
	}

	public String getTitel() {
		if (titel == null)
			return "";
		else
			return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public Set<Benutzer> getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(Set<Benutzer> benutzer) {
		this.benutzer = benutzer;
	}

	public List<Benutzer> getBenutzerList() {
		if (benutzer == null)
			return new ArrayList<Benutzer>();
		else
			return new ArrayList<Benutzer>(benutzer);
	}

	public Set<Schritt> getSchritte() {
		return schritte;
	}

	public void setSchritte(Set<Schritt> schritte) {
		this.schritte = schritte;
	}

	public int getSchritteSize() {
		if (schritte == null)
			return 0;
		else
			return schritte.size();
	}

	public List<Schritt> getSchritteList() {
		if (schritte == null)
			schritte = new HashSet<Schritt>();
		return new ArrayList<Schritt>(schritte);
	}

	public boolean isPanelAusgeklappt() {
		return panelAusgeklappt;
	}

	public void setPanelAusgeklappt(boolean panelAusgeklappt) {
		this.panelAusgeklappt = panelAusgeklappt;
	}

}
