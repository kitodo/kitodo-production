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
import javax.persistence.ForeignKey;
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
@Table(name = "template")
public class Vorlage implements Serializable {
	private static final long serialVersionUID = 1736135433162833277L;

	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

	@Column(name = "origin")
	private String herkunft;

	@ManyToOne
	@JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_template_process_id"))
	private Prozess prozess;

	@OneToMany(mappedBy = "vorlage", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("title ASC")
	private Set<Vorlageeigenschaft> eigenschaften;

	@Transient
	private boolean panelAusgeklappt = true;

	public Vorlage() {
		this.eigenschaften = new HashSet<Vorlageeigenschaft>();
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

	public Set<Vorlageeigenschaft> getEigenschaften() {
		return this.eigenschaften;
	}

	public void setEigenschaften(Set<Vorlageeigenschaft> eigenschaften) {
		this.eigenschaften = eigenschaften;
	}

	/*
	 * ##########################################################################################################
	 * ## ## Helper ##
	 * #########################################################################################################
	 */

	public String getHerkunft() {
		return this.herkunft;
	}

	public void setHerkunft(String herkunft) {
		this.herkunft = herkunft;
	}

	/**
	 *
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
	 *
	 * @return add description
	 */
	public List<Vorlageeigenschaft> getEigenschaftenList() {
		try {
			Hibernate.initialize(this.eigenschaften);
		} catch (HibernateException e) {
		}
		if (this.eigenschaften == null) {
			return new ArrayList<Vorlageeigenschaft>();
		}
		return new ArrayList<Vorlageeigenschaft>(this.eigenschaften);
	}

}
