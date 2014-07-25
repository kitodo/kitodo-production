package de.sub.goobi.beans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;

/**
 * Usergroups owning different access rights, represented by integer values
 * 
 * 1: Administration - can do anything 2: Projectmanagement - may do a lot (but
 * not user management, no user switch, no administrative form) 3: User and
 * process (basically like 4 but can be used for setting aditional boundaries
 * later, if so desired) 4: User only: can see current steps
 * 
 * ================================================================
 */
public class Benutzergruppe implements Serializable, Comparable<Benutzergruppe> {
	private static final long serialVersionUID = -5924845694417474352L;
	private Integer id;
	private String titel;
	private Integer berechtigung;
	private Set<Benutzer> benutzer;
	private Set<Schritt> schritte;
	private boolean panelAusgeklappt = false;

	public Benutzergruppe() {
		this.schritte = new HashSet<Schritt>();
		this.benutzer = new HashSet<Benutzer>();
	}

	/*
	 * Getter und Setter
	 */

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBerechtigung() {
		if (this.berechtigung == null) {
			this.berechtigung = 4;
		} else if (this.berechtigung == 3) {
			this.berechtigung = 4;
		}
		return this.berechtigung;
	}

	public void setBerechtigung(int berechtigung) {
		this.berechtigung = berechtigung;
	}

	public String getBerechtigungAsString() {
		if (this.berechtigung == null) {
			this.berechtigung = 4;
		} else if (this.berechtigung == 3) {
			this.berechtigung = 4;
		}
		return String.valueOf(this.berechtigung.intValue());
	}

	public void setBerechtigungAsString(String berechtigung) {
		this.berechtigung = Integer.parseInt(berechtigung);
	}

	public String getTitel() {
		if (this.titel == null) {
			return "";
		} else {
			return this.titel;
		}
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public Set<Benutzer> getBenutzer() {
		return this.benutzer;
	}

	public void setBenutzer(Set<Benutzer> benutzer) {
		this.benutzer = benutzer;
	}

	public List<Benutzer> getBenutzerList() {
		try {
			Hibernate.initialize(getBenutzer());
		} catch (org.hibernate.HibernateException e) {

		}

		if (this.benutzer == null) {
			return new ArrayList<Benutzer>();
		} else {
			return new ArrayList<Benutzer>(this.benutzer);
		}
	}

	public Set<Schritt> getSchritte() {
		return this.schritte;
	}

	public void setSchritte(Set<Schritt> schritte) {
		this.schritte = schritte;
	}

	public int getSchritteSize() {
		Hibernate.initialize(getSchritte());
		if (this.schritte == null) {
			return 0;
		} else {
			return this.schritte.size();
		}
	}

	public List<Schritt> getSchritteList() {
		Hibernate.initialize(getSchritte());
		if (this.schritte == null) {
			this.schritte = new HashSet<Schritt>();
		}
		return new ArrayList<Schritt>(this.schritte);
	}

	public boolean isPanelAusgeklappt() {
		return this.panelAusgeklappt;
	}

	public void setPanelAusgeklappt(boolean panelAusgeklappt) {
		this.panelAusgeklappt = panelAusgeklappt;
	}

	@Override
	public int compareTo(Benutzergruppe o) {
		return this.getTitel().compareTo(o.getTitel());
	}

	
	@Override
	public boolean equals(Object obj) {
		return this.getTitel().equals(((Benutzergruppe)obj).getTitel());
	}
}
