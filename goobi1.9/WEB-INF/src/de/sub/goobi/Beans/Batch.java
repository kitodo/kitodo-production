package de.sub.goobi.Beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.goobi.production.api.property.xmlbasedprovider.Status;

import de.sub.goobi.Beans.Property.DisplayPropertyList;
import de.sub.goobi.Beans.Property.IGoobiEntity;
import de.sub.goobi.Beans.Property.IGoobiProperty;

public class Batch implements Serializable, IGoobiEntity {

	private static final long serialVersionUID = -8503827557959453247L;
	private Integer batchId;
	private String title;
	private List<Prozess> batchList = new ArrayList<Prozess>();
	private Projekt projekt;
	private Set<BatchProperty> properties;
	private DisplayPropertyList displayProperties;

	public Batch() {
		title = "";
		properties = new HashSet<BatchProperty>();
	}

	public void setBatchId(Integer batchId) {
		this.batchId = batchId;
	}

	public Integer getBatchId() {
		return batchId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setBatchList(List<Prozess> batchList) {
		this.batchList = batchList;
	}

	public List<Prozess> getBatchList() {
		return batchList;
	}

	public void setProjekt(Projekt projekt) {
		this.projekt = projekt;
	}

	public Projekt getProjekt() {
		return projekt;
	}

	@Override
	public Status getStatus() {
		return Status.getBatchStatus(this);
	}

	@Override
	public List<IGoobiProperty> getProperties() {
		List<IGoobiProperty> returnlist = new ArrayList<IGoobiProperty>();
		returnlist.addAll(getEigenschaftenList());
		return returnlist;
	}

	public List<BatchProperty> getEigenschaftenList() {
		if (properties == null) {
			return new ArrayList<BatchProperty>();
		} else {
			return new ArrayList<BatchProperty>(properties);
		}
	}

	@Override
	public void addProperty(IGoobiProperty toAdd) {
		properties.add((BatchProperty) toAdd);

	}

	@Override
	public void removeProperty(IGoobiProperty toRemove) {
		properties.remove(toRemove);
		toRemove.setOwningEntity(null);
	}

	public DisplayPropertyList getDisplayProperties() {
		if (displayProperties == null) {
			displayProperties = new DisplayPropertyList(this);
		}
		return displayProperties;
	}

	@Override
	public void refreshProperties() {
		displayProperties = null;
	}

	@Override
	public Integer getId() {
		return batchId;
	}

}
