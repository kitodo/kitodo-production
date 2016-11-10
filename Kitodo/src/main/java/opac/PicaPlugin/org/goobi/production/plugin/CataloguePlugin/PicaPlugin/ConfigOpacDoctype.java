/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
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
