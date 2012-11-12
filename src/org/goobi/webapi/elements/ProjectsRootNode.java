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

package org.goobi.webapi.elements;

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