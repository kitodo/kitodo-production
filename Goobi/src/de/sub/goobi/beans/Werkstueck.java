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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.hibernate.HibernateException;

public class Werkstueck implements Serializable {
	private static final long serialVersionUID = 123266825187246791L;
	private Integer id;
	private Prozess prozess;
	private Set<Werkstueckeigenschaft> eigenschaften;
	private boolean panelAusgeklappt = true;

	public Werkstueck() {
		this.eigenschaften = new HashSet<Werkstueckeigenschaft>();
	}

	/*
	 * ##################################################### ##################################################### ## ## Getter und Setter ##
	 * ##################################################### ####################################################
	 */

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Prozess getProzess() {
		return this.prozess;
	}

	public void setProzess(Prozess prozess) {
		this.prozess = prozess;
	}

	public boolean isPanelAusgeklappt() {
		return this.panelAusgeklappt;
	}

	public void setPanelAusgeklappt(boolean panelAusgeklappt) {
		this.panelAusgeklappt = panelAusgeklappt;
	}

	public Set<Werkstueckeigenschaft> getEigenschaften() {
		return this.eigenschaften;
	}

	public void setEigenschaften(Set<Werkstueckeigenschaft> eigenschaften) {
		this.eigenschaften = eigenschaften;
	}

	/*
	 * ##################################################### ##################################################### ## ## Helper ##
	 * ##################################################### ####################################################
	 */

	public int getEigenschaftenSize() {
		try {
			Hibernate.initialize(this.eigenschaften);
		} catch (HibernateException e) {
		}
		if (this.eigenschaften == null) {
			return 0;
		} else {
			return this.eigenschaften.size();
		}
	}

	public List<Werkstueckeigenschaft> getEigenschaftenList() {
		try {
			Hibernate.initialize(this.eigenschaften);
		} catch (HibernateException e) {
		}
		if (this.eigenschaften == null) {
			return new ArrayList<Werkstueckeigenschaft>();
		}
		return new ArrayList<Werkstueckeigenschaft>(this.eigenschaften);
	}
}
