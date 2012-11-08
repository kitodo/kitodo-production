package org.goobi.webapi.beans;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.sub.goobi.beans.Projekt;

/**
 * The ProjectsRootNode class is necessary to control the XML root element’s
 * name to be ‘projects’. Simply annotating the de.sub.goobi.beans.Projekt class
 * with @XmlRootElement(name = "project") results in a wrapping element named
 * <projekts> who’s name is still derived from the classes’ name, not from the
 * ‘name’ property set in the annotation and cannot be changed otherwise.
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
@XmlRootElement(name = "projects")
public class ProjectsRootNode {
	@SuppressWarnings("unused")
	@XmlElement(name = "project")
	private ArrayList<Projekt> projects;

	public ProjectsRootNode() { // stupid Jersey API requires no-arg default constructor which is never used
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public ProjectsRootNode(Collection<Projekt> data) {
		projects = new ArrayList<Projekt>(data);
	}

}