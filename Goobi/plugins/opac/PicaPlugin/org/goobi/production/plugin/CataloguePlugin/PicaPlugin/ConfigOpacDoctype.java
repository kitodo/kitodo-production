/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.util.ArrayList;

class ConfigOpacDoctype {
	private final String title;
	private final boolean periodical;
	private final boolean multiVolume;
	private final boolean containedWork;
	private final ArrayList<String> mappings;

	ConfigOpacDoctype(String inTitle, boolean periodical, boolean multiVolume, boolean containedWork,
			ArrayList<String> mappings) {
		this.title = inTitle;
		this.periodical = periodical;
		this.multiVolume = multiVolume;
		this.containedWork = containedWork;
		this.mappings = mappings;
	}

	public String getTitle() {
		return this.title;
	}

	boolean isPeriodical() {
		return periodical;
	}

	boolean isMultiVolume() {
		return multiVolume;
	}

	boolean isContainedWork() {
		return containedWork;
	}

	ArrayList<String> getMappings() {
		return mappings;
	}

}
