/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package de.sub.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

@Entity
@Table(name = "workpiece")
public class Werkstueck implements Serializable {
	private static final long serialVersionUID = 123266825187246791L;

	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "process_id")
	private Prozess prozess;

	@OneToMany(mappedBy = "werkstueck", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("title ASC")
	private Set<Werkstueckeigenschaft> eigenschaften;

	@Transient
	private boolean panelAusgeklappt = true;

	public Werkstueck() {
		this.eigenschaften = new HashSet<Werkstueckeigenschaft>();
	}

	/*
	 * ##########################################################################################################
	 * ## ## Getter und Setter ##
	 * #########################################################################################################
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
	 * ##########################################################################################################
	 * ## ## Helper ##
	 * #########################################################################################################
	 */

	/**
	 * @return add description
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

	/**
	 * @return add description
	 */
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
