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

package org.goobi.production.enums;

public enum PluginType {

	Import(1, "import"), Step(2, "step");
	
	private int id;
	private String name;
	
	private PluginType(int id, String name) {
		this.setId(id);
		this.setName(name);
	}
	
	
	public static PluginType getTypeFromValue(String pluginType) {
		if (pluginType != null) {
			for (PluginType type : PluginType.values()) {
				if (type.getName().equals(pluginType))
					return type;
			}
		}
		return null;
	}
	
	public static PluginType getTypesFromId(int pluginType) {
		for (PluginType type : PluginType.values()) {
			if (type.getId()== pluginType) {
				return type;
			}
		}
		return null;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}


	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
}
