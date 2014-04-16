package org.goobi.production.constants;

/**
 * 
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
	 * Points to a folder on the file system that contains Production plugin
	 * jars. In the folder, there must be subfolders named as defined in enum
	 * PluginType (currently: “import”, “step”, “validation”, “command” and
	 * “opac”) in which the plugin jars must be stored.
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

}
