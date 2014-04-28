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
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package org.goobi.production.constants;

/**
 * These constants define configuration parameters usable in the configuration
 * file.
 * 
 * TODO: Make all string literals throughout the code constants here.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Parameters {
	/**
	 * Milliseconds. Indicates the maximum duration an interaction with a
	 * library catalogue may take. Defaults to 30 minutes.
	 */
	public static final String CATALOGUE_TIMEOUT = "catalogue.timeout";

	/**
	 * Points to a folder on the file system that contains Production
	 * configuration files.
	 */
	public static final String CONFIG_DIR = "KonfigurationVerzeichnis";

	/**
	 * Integer. Number of hits to show per page on the hitlist when multiple
	 * hits were found on a catalogue search.
	 */
	public static final String HITLIST_PAGE_SIZE = "catalogue.hitlist.pageSize";

	/**
	 * Points to a folder on the file system that contains Production plug-in
	 * jars. In the folder, there must be subfolders named as defined in enum
	 * PluginType (currently: “import”, “step”, “validation”, “command” and
	 * “opac”) in which the plug-in jars must be stored.
	 * 
	 * <p>
	 * Must be terminated by the file separator.
	 * </p>
	 * 
	 * @see org.goobi.production.enums.PluginType
	 */
	// TODO: Some of the old code doesn’t yet use
	// org.apache.commons.io.FilenameUtils for path management which causes
	// paths not ending in the file separator not to work. Use the library
	// for any path handling. It does it less error prone.
	public static final String PLUGIN_FOLDER = "pluginFolder";

	/**
	 * Points to a folder on the file system that plug-ins may use to write
	 * temporary files.
	 */
	public static final String PLUGIN_TEMP_DIR = "debugFolder";

}
