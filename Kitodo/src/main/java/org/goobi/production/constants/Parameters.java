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

/**
 * These constants define configuration parameters usable in the configuration
 * file. TODO: Make all string literals throughout the code constants here.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Parameters {
    /**
     * Content to put in the URI field when adding a new metadata element of
     * type person. This should usually be your preferred norm data file’s URI
     * prefix as to the user doesn’t have to enter it over and over again.
     *
     * <p>
     * Example: authority.default=http\://d-nb.info/gnd/
     * </p>
     */
    public static final String AUTHORITY_DEFAULT = "authority.default";

    /**
     * Which authority identifier to use for a given URI prefix.
     *
     * <p>
     * Example: authority.http\://d-nb.info/gnd/.id=gnd
     * </p>
     */
    public static final String AUTHORITY_ID_FROM_URI = "authority.{0}.id";

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
     * Comma-separated list of Strings which may be enclosed in double quotes.
     * Separators available for double page pagination modes.
     */
    public static final String PAGE_SEPARATORS = "pageSeparators";

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
     * Private constructor to hide the implicit public one.
     */
    private Parameters() {

    }
}
