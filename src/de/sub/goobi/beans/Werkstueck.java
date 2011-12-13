package de.sub.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.goobi.production.api.property.xmlbasedprovider.Status;

import de.sub.goobi.beans.property.DisplayPropertyList;
import de.sub.goobi.beans.property.IGoobiEntity;
import de.sub.goobi.beans.property.IGoobiProperty;

public class Werkstueck implements Serializable, IGoobiEntity {
	private static final long serialVersionUID = 123266825187246791L;
	private Integer id;
	private Prozess prozess;
	private Set<Werkstueckeigenschaft> eigenschaften;
	private DisplayPropertyList displayProperties;

	private boolean panelAusgeklappt = true;

	public Werkstueck() {
		eigenschaften = new HashSet<Werkstueckeigenschaft>();
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

	public Prozess getProzess() {
		return prozess;
	}

	public void setProzess(Prozess prozess) {
		this.prozess = prozess;
	}

	public boolean isPanelAusgeklappt() {
		return panelAusgeklappt;
	}

	public void setPanelAusgeklappt(boolean panelAusgeklappt) {
		this.panelAusgeklappt = panelAusgeklappt;
	}

	public Set<Werkstueckeigenschaft> getEigenschaften() {
		return eigenschaften;
	}

	public void setEigenschaften(Set<Werkstueckeigenschaft> eigenschaften) {
		this.eigenschaften = eigenschaften;
	}

	/*
	 * ##################################################### ##################################################### ## ## Helper ##
	 * ##################################################### ####################################################
	 */

	public int getEigenschaftenSize() {
		if (eigenschaften == null)
			return 0;
		else
			return eigenschaften.size();
	}

	public List<Werkstueckeigenschaft> getEigenschaftenList() {
		if (eigenschaften == null)
			return new ArrayList<Werkstueckeigenschaft>();
		return new ArrayList<Werkstueckeigenschaft>(eigenschaften);
	}

	public Status getStatus() {
		return Status.getProductStatusFromEntity(this);
	}

	public List<IGoobiProperty> getProperties() {
		List<IGoobiProperty> returnlist = new ArrayList<IGoobiProperty>();
		returnlist.addAll(getEigenschaftenList());
		return returnlist;
	}
	
	public void addProperty(IGoobiProperty toAdd) {
		eigenschaften.add((Werkstueckeigenschaft) toAdd);
	}
	
	
	public void removeProperty(IGoobiProperty toRemove) {
		getEigenschaften().remove(toRemove);
		toRemove.setOwningEntity(null);
		
	}
	
	/**
	 * 
	 * @return instance of {@link DisplayPropertyList}
	 */
	public DisplayPropertyList getDisplayProperties() {
		if (displayProperties == null) {
			displayProperties = new DisplayPropertyList(this);
		}
		return displayProperties;
	}
	
	public void refreshProperties() {
		displayProperties = null;
	}
}
