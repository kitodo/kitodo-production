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

package org.goobi.production.constants;

import java.util.*;
import java.util.Map.Entry;

/**
 * These constants define configuration parameters usable in the configuration
 * file.
 * 
 * TODO: Make all string literals throughout the code constants here.
 * 
 * @author Matthias Ronge
 */
public class Parameters {
	/**
	 * Configuration to use.
	 */
	private Map<String, String> config;

	/**
	 * Creates a new parameters accessor class.
	 * 
	 * @param config
	 *            configuration to use
	 */
	public Parameters(Map<String, String> config) {
		this.config = config;
	}

	/**
	 * String to append to a record identifier for this authority in order to
	 * form a URL usable to actually retrieve data.
	 * <p>
	 * Example:
	 * {@code authority.http\://d-nb.info/gnd/.dataUrlTail=/about/lds.rdf}
	 */
	private static final String AUTHORITY_DATA_URL_TAIL = "authority.{0}.dataUrlTail";
	
	/**
	 * Content to put in the URI field when adding a new metadata element of
	 * type person. This should usually be your preferred norm data file’s URI
	 * prefix as to the user doesn’t have to enter it over and over again.
	 * <p>
	 * Example: {@code authority.default=http\://d-nb.info/gnd/}
	 */
	public static final String AUTHORITY_DEFAULT = "authority.default";

	/**
	 * When loading norm data records, update the meta-data type with the value
	 * found following the specified path through the linked data graph. See
	 * {@link org.kitodo.production.lugh.ld.GraphPath} for a powerful graph path
	 * example.
	 */
	private static final String AUTHORITY_MAPPING = "authorityMapping.";

	/**
	 * List of meta-data types that, if included in a meta-data group, will
	 * contain the authority record that is the primary reference for the
	 * thing described.
	 */
	public static final String AUTHORITY_RECORD_URI_FIELD = "authority.valueMetaData";
	
	/**
	 * Integer, limits the number of batches showing on the page “Batches”.
	 * Defaults to -1 which disables this functionality. If set, only the
	 * limited number of batches will be shown, the other batches will be
	 * present but hidden and thus cannot be modified and not even be deleted.
	 */
	public static final String BATCH_DISPLAY_LIMIT = "batchMaxSize";

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
	 * Whether during an export to the DMS the images will be copied. Defaults
	 * to true.
	 */
	public static final String EXPORT_WITH_IMAGES = "automaticExportWithImages";

	/**
	 * Integer. Number of hits to show per page on the hitlist when multiple
	 * hits were found on a catalogue search.
	 */
	public static final String HITLIST_PAGE_SIZE = "catalogue.hitlist.pageSize";

	/**
	 * Long. Number of pages per process below which the features in the
	 * granualarity dialog shall be locked.
	 */
	public static final String MINIMAL_NUMBER_OF_PAGES = "numberOfPages.minimum";

	/**
	 * A map of namespace prefixes to namespaces. The constant has to be
	 * followed by a dot and the prefix, the right hand side of the assignment
	 * is the URL to abbreviate. For {@code #}-namespaces, the {@code #} must be
	 * written down.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * namespace.agora=http://www.agora-exchange.org/XML#
	 * namespace.dbpedia=http://dbpedia.org/resource/
	 * namespace.dc=http://purl.org/dc/elements/1.1/
	 * namespace.dcmitype=http://purl.org/dc/dcmitype/
	 * namespace.dcterms=http://purl.org/dc/terms/
	 * namespace.dnbt=http://d-nb.info/standards/elementset/dnb#
	 * namespace.filmportal=http://www.filmportal.de/person/
	 * namespace.foaf=http://xmlns.com/foaf/0.1/
	 * namespace.gdz=http://gdz.sub.uni-goettingen.de/
	 * namespace.gnd=http://d-nb.info/gnd/
	 * namespace.gndac=http://d-nb.info/standards/vocab/gnd/geographic-area-code#
	 * namespace.gndg=http://d-nb.info/standards/vocab/gnd/gender#
	 * namespace.gndo=http://d-nb.info/standards/elementset/gnd#
	 * namespace.gndsc=http://d-nb.info/standards/vocab/gnd/gnd-sc#
	 * namespace.goobi=http://meta.goobi.org/v1.5.1/
	 * namespace.gpath=http://names.zeutschel.de/GraphPath/v1#
	 * namespace.lang=http://id.loc.gov/vocabulary/iso639-2/
	 * namespace.mets=http://www.loc.gov/METS/
	 * namespace.mix=http://www.loc.gov/mix/v10#
	 * namespace.mods=http://www.loc.gov/mods/v3#
	 * namespace.owl=http://www.w3.org/2002/07/owl#
	 * namespace.premis=info:lc/xmlns/premis-v2#
	 * namespace.rdf=http://www.w3.org/1999/02/22-rdf-syntax-ns#
	 * namespace.rdfs=http://www.w3.org/2000/01/rdf-schema#
	 * namespace.schema=http://schema.org/
	 * namespace.viaf=http://viaf.org/viaf/
	 * namespace.wpde=http://de.wikipedia.org/wiki/
	 * namespace.xsd=http://www.w3.org/2001/XMLSchema#
	 * </pre>
	 */
	private static final String NAMESPACE_MAP = "namespace.";

	/**
	 * Comma-separated list of Strings which may be enclosed in double quotes.
	 * Separators available for double page pagination modes.
	 */
	public static final String PAGE_SEPARATORS = "pageSeparators";

	/**
	 * Points to a folder on the file system that contains Production plug-in
	 * jars. In the folder, there must be subfolders named as defined in enum
	 * PluginType (currently: “import”, “step”, “validation”, “command” and
	 * “opac”) in which the plug-in jars must be stored.
	 * <p>
	 * Must be terminated by the file separator.
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

	/**
	 * Boolean. Set to true to enable the feature of automatic meta data
	 * inheritance and enrichment. If this is enabled, all meta data elements
	 * from a higher level of the logical document structure are automatically
	 * inherited and lower levels are enriched with them upon process creation,
	 * given they have the same meta data type addable. Defaults to false.
	 */
	public static final String USE_METADATA_ENRICHMENT = "useMetadataEnrichment";

	/**
	 * Boolean. Set to false to disable automatic pagination changes in the
	 * metadata editor. If false, pagination must be updated manually by
	 * clicking the link “Read in pagination from images”.
	 */
	public static final String WITH_AUTOMATIC_PAGINATION = "MetsEditorWithAutomaticPagination";

	/**
	 * Points to a folder on the file system that XSLT files are stored which are used
	 * to transform the "XML log" (as visible from the XML button in the processes
	 * list) to a downloadable PDF docket which can be enclosed with the physical
	 * binding units to digitise. XSL transformation scripts used by plugins
	 * are also located here.
	 */
	public static final String XSLT_DIR = "xsltFolder";

	/**
	 * Returns the tail to append to an authority data URL for retrieval, if
	 * any, or {@code null}.
	 * 
	 * @param authority
	 *            authority to look up a tail for
	 * @return URL tail, {@code null} otherwise
	 */
	public String getAuthorityDataURLTail(String authority) {
		String prefix = this.getNamespacePrefixes(false, false).get(authority);
		String key = AUTHORITY_DATA_URL_TAIL.replace("{0}", prefix != null ? prefix : authority);
		return config.get(key);
	}

	/**
	 * Returns a mapping from meta-data fields to graph paths, used to import
	 * authority records.
	 * 
	 * @return authority mapping
	 */
	public Map<String, String> getAuthorityMapping() {
		Map<String, String> result = new HashMap<>();
		int beginIndex = AUTHORITY_MAPPING.length();
		for (Entry<String, String> entry : config.entrySet()) {
			if (entry.getKey().startsWith(AUTHORITY_MAPPING)) {
				result.put(entry.getKey().substring(beginIndex), entry.getValue());
			}
		}
		return result;
	}

	/**
	 * Returns the map of configured namespace prefixes. A map can either be
	 * requested to resolve prefixes, or to look up prefixes. Hash namespaces
	 * can be included either without hash (for XML) or with hash (for linked
	 * data).
	 * 
	 * @param resolve
	 *            if true, mapping is from prefix to namespace, if false,
	 *            mapping is from namespace to prefix
	 * @param hash
	 *            if true, hash namespaces end with hash, if false, they end
	 *            without hash
	 * @return map of configured namespaces
	 */
	public Map<String, String> getNamespacePrefixes(boolean resolve, boolean hash) {
		Map<String, String> result = new HashMap<>();
		int beginIndex = NAMESPACE_MAP.length();
		for(Entry<String, String> entry:config.entrySet()){
			String key = entry.getKey();
			if(key.startsWith(NAMESPACE_MAP)){
				String prefix = key.substring(beginIndex);
				String namespace = config.get(key);
				if(!namespace.endsWith("/")){
					if(hash){
						if(! namespace.endsWith("#")) {
							namespace = namespace.concat("#");
						}
					}else{
						if(namespace.endsWith("#")){
							namespace = namespace.substring(0, namespace.length() - 1);
						}
					}
				}
				if(resolve){
					result.put(prefix, namespace);
				}else{
					result.put(namespace, prefix);
				}
			}
		}
		return result;
	}

}
