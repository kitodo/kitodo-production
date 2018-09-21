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

package org.kitodo.config.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.kitodo.exceptions.ConfigParameterException;

/**
 * These constants define configuration parameters usable in the configuration
 * file. This file reflects the order of the global
 * {@code kitodo_config.properties} file (as far as possible), and vice versa.
 */
public enum ParameterCore implements ParameterInterface {

    /*
     * FILE AND DIRECTORY MANAGEMENT
     *
     * Directories
     */

    /*
     * Parameters.java has been sorted to corresponding to kitodo_config.properties,
     * including the section headers as you see. However there is an entry
     * KonfigurationVerzeichnis in kitodo_config.properties, but there is no
     * constant for it here. This comment is to explain where the constant is if
     * someone compares the two files in the future:
     */

    DIR_XML_CONFIG("KonfigurationVerzeichnis", null, null, new ArrayList<>()),

    /**
     * Absolute path to the directory that the rule set definition files will be
     * read from. It must be terminated by a directory separator ("/").
     */
    DIR_RULESETS("RegelsaetzeVerzeichnis", null, null, new ArrayList<>()),

    /**
     * Absolute path to the directory that XSLT files are stored in which are used
     * to transform the "XML log" (as visible from the XML button in the processes
     * list) to a downloadable PDF docket which can be enclosed with the physical
     * binding units to digitise. The path must be terminated by a directory
     * separator ("/").
     */
    DIR_XSLT("xsltFolder", null, null, new ArrayList<>()),

    /*
     * Parameters.java has been sorted to corresponding to kitodo_config.properties,
     * including the section headers as you see. However there is an entry
     * MetadatenVerzeichnis in kitodo_config.properties, but there is no constant
     * for it here. This comment is to explain where the constant is if someone
     * compares the two files in the future:
     */

    DIR_PROCESSES("MetadatenVerzeichnis", null, null, new ArrayList<>()),

    /**
     * Absolute path to the base directory of the users' home directories,
     * terminated by a directory separator ("/"). If a user accepts a task to work
     * on which will require him or her to have access permission to the data of a
     * process, a symbolic link to the process directory in question will be created
     * in his or her home directory that will be removed again after finishing the
     * task. Note: If LDAP is used, the users' home dirs will instead be read from
     * LDAP
     */
    DIR_USERS("dir_Users", null, null, new ArrayList<>()),

    /**
     * Absolute path to a folder the application can temporarily create files in,
     * terminated by a directory separator ("/").
     */
    DIR_TEMP("tempfolder", String.class, "/usr/local/kitodo/temp/", new ArrayList<>()),

    /**
     * Path to directory in which BPMN diagrams are stored.
     */
    DIR_DIAGRAMS("diagramsFolder", null, null, new ArrayList<>()),

    /*
     * Parameters.java has been sorted to corresponding to kitodo_config.properties,
     * including the section headers as you see. However there is an entry
     * moduleFolder in kitodo_config.properties, but there is no constant for it
     * here. This comment is to explain where the constant is if someone compares
     * the two files in the future:
     */

    DIR_MODULES("moduleFolder", null, null, new ArrayList<>()),

    /**
     * Points to a folder on the file system that contains <b>legacy</b> Production
     * plug-in jars. In the folder, there must be subfolders named as defined in
     * enum PluginType (currently: “import” and “opac”) in which the plug-in jars
     * must be stored.
     *
     * <p>
     * Must be terminated by the file separator.
     */
    DIR_PLUGINS("pluginFolder", null, null, new ArrayList<>()),

    /**
     * Points to a folder on the file system that <b>legacy</b> plug-ins may use to
     * write temporary files.
     */
    DIR_PLUGINS_TEMP("debugFolder", null, null, new ArrayList<>()),

    /*
     * Directory management
     */

    /**
     * Boolean, defaults to {@code false}.
     */
    CREATE_ORIG_FOLDER_IF_NOT_EXISTS("createOrigFolderIfNotExists", Boolean.TYPE, false, new ArrayList<>()),

    /*
     * Parameters.java has been sorted to corresponding to kitodo_config.properties,
     * including the section headers as you see. However there is an entry
     * createSourceFolder in kitodo_config.properties, but there is no constant for
     * it here. This comment is to explain where the constant is if someone compares
     * the two files in the future:
     */
    CREATE_SOURCE_FOLDER("createSourceFolder", null, null, new ArrayList<>()),

    /**
     * Prefix of image directory name created on process creation.
     */
    DIRECTORY_PREFIX("DIRECTORY_PREFIX", String.class, "orig", new ArrayList<>()),

    /**
     * Directory suffix for created image directory on process creation.
     */
    DIRECTORY_SUFFIX("DIRECTORY_SUFFIX", String.class, "tif", new ArrayList<>()),

    /**
     * Boolean, defaults to {@code false}.
     */
    IMPORT_USE_OLD_CONFIGURATION("importUseOldConfiguration", Boolean.TYPE, false, new ArrayList<>()),

    /**
     * Creation and export of process sub-directories, e.g.
     * {@code images/(processtitle)_tif&ocr/(processtitle)_pdf}.
     * {@code (processtitle)} is a placeholder for the process title If you comment
     * in the parameter processDirs without a value, the result is that the whole
     * process directory will be exported and no directory well be created. If you
     * leave the parameter commented out, the whole functionality is disabled. Using
     * the {@code processDirs} parameter is always an addition to the existing
     * folder creating and exporting functions of Kitodo.Production.
     */
    PROCESS_DIRS("processDirs", null, null, new ArrayList<>()),

    /**
     * Set if master images folder {@code orig_} should be used at all. Boolean,
     * defaults to {@code true}.
     */
    USE_ORIG_FOLDER("useOrigFolder", Boolean.TYPE, true, new ArrayList<>()),

    /*
     * Directory and symbolic link management
     */

    /**
     * Script to create the user's home directory when adding a new user.
     */
    SCRIPT_CREATE_DIR_USER_HOME("script_createDirUserHome", null, null, new ArrayList<>()),

    /**
     * Script to create the directory for a new process.
     */
    SCRIPT_CREATE_DIR_META("script_createDirMeta", null, null, new ArrayList<>()),

    /**
     * Script to create a symbolic link in the user home directory and set
     * permissions for the user.
     */
    SCRIPT_CREATE_SYMLINK("script_createSymLink", null, null, new ArrayList<>()),

    /**
     * Script to remove the symbolic link from the user home directory.
     */
    SCRIPT_DELETE_SYMLINK("script_deleteSymLink", null, null, new ArrayList<>()),

    /*
     * Runnotes
     */

    /**
     * Filename of the XSLT file for transforming old metadata files which need to
     * be in the xslt folder above.
     */
    XSLT_FILENAME_METADATA_TRANSFORMATION("xsltFilenameMetadataTransformation", null, null, new ArrayList<>()),

    /*
     * Images
     */

    /**
     * Prefix for image names as regex. Default is 8 digits \\d{8} and gets
     * validated.
     */
    IMAGE_PREFIX("ImagePrefix", String.class, "\\d{8}", new ArrayList<>()),

    /**
     * Sorting of images.
     * <p>
     * Numeric sorting of images. 1 is lesser then 002, compares the number of image
     * names, characters other than digits are not supported.
     * </p>
     * <p>
     * Alphanumeric sorting of images. 1 is greater then 002, compares character by
     * character of image names, all characters are supported.
     * </p>
     */
    IMAGE_SORTING("ImageSorting", String.class, "number", Arrays.asList("number", "alphanumeric")),

    /**
     * Defaults to {@code fertig/}.
     */
    DONE_DIRECTORY_NAME("doneDirectoryName", String.class, "fertig/", new ArrayList<>()),

    /*
     * VISUAL APPEARANCE
     *
     * Internationalization
     */

    /**
     * Absolute path to the directory that the resource bundle files are stored in,
     * terminated by a directory separator ("/").
     *
     * <p>
     * Note: If this directory DOESN'T EXIST, the internal resource bundles will be
     * used. If this directory exists BUT DOES NOT CONTAIN suitable resources, the
     * screens will not work as expected.
     */
    DIR_LOCAL_MESSAGES("localMessages", String.class, "/usr/local/kitodo/messages/", new ArrayList<>()),

    /**
     * Start-up language: If not set, Kitodo.Production will start up with the
     * language best matching the user's Accept-Languages HTTP Request header. You
     * can override this behaviour by setting a default language here.
     */
    LANGUAGE_FORCE_DEFAULT("language.force-default", String.class, "de", new ArrayList<>()),

    /**
     * If no Accept-Language Http Request header is present, use the following
     * language.
     */
    LANGUAGE_DEFAULT("language.default", String.class, "de", new ArrayList<>()),

    /*
     * Data protection
     */

    /**
     * The General Data Protection Regulation or local law might require to set this
     * value to true. anonymized statistics, displaying user on steps, etc. Boolean,
     * defaults to {@code false}.
     */
    ANONYMIZE("anonymize", Boolean.TYPE, false, new ArrayList<>()),

    /**
     * Enable / disable search for steps done by user. Boolean, defaults to
     * {@code false}.
     */
    WITH_USER_STEP_DONE_SEARCH("withUserStepDoneSearch", Boolean.TYPE, false, new ArrayList<>()),

    /*
     * METADATA PROCESSING
     *
     * Catalogue search
     */

    /**
     * Number of hits to show per page on the hitlist when multiple hits were found
     * on a catalogue search. Integer, defaults to 12.
     */
    HITLIST_PAGE_SIZE("catalogue.hitlist.pageSize", Integer.TYPE, 12, new ArrayList<>()),

    /**
     * Indicates the maximum duration an interaction with a library catalogue may
     * take. Milliseconds, defaults to 30 minutes.
     */
    CATALOGUE_TIMEOUT("catalogue.timeout", Long.class, TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES),
            new ArrayList<>()),

    /*
     * Metadata editor behavior
     */

    /**
     * Long, value in milliseconds. Defaults to 180000 (30 minutes).
     */
    METS_EDITOR_LOCKING_TIME("metsEditor.lockingTime", Long.TYPE, TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES),
            new ArrayList<>()),

    /**
     * Use special image folder for METS editor if exists (define suffix here).
     */
    METS_EDITOR_DEFAULT_SUFFIX("metsEditor.defaultSuffix", null, null, new ArrayList<>()),

    /**
     * Enables or disables automatic pagination changes in the meta-data editor. If
     * false, pagination must be updated manually by clicking the link “Read in
     * pagination from images”. Boolean, defaults to {@code true}.
     */
    WITH_AUTOMATIC_PAGINATION("metsEditor.useAutomaticPagination", Boolean.TYPE, true, new ArrayList<>()),

    /**
     * Use special pagination type for automatic default pagination.
     */
    METS_EDITOR_DEFAULT_PAGINATION("metsEditor.defaultPagination", null, "uncounted",
            Arrays.asList("arabic", "roman", "uncounted")),

    /**
     * Use a maximum of characters to display titles in the left part of mets
     * editor. Integer, the default value is 0 (everything is displayed).
     */
    METS_EDITOR_MAX_TITLE_LENGTH("metsEditor.maxTitleLength", null, null, new ArrayList<>()),

    /**
     * Initialize all sub elements in METS editor to assign default values. Boolean,
     * defaults to {@code true}.
     */
    METS_EDITOR_ENABLE_DEFAULT_INITIALISATION("metsEditor.enableDefaultInitialisation", Boolean.TYPE, true, new ArrayList<>()),

    /**
     * Display the file manipulation dialog within the METS editor.
     */
    METS_EDITOR_DISPLAY_FILE_MANIPULATION("metsEditor.displayFileManipulation", null, null, new ArrayList<>()),

    /**
     * Comma-separated list of Strings which may be enclosed in double quotes.
     * Separators available for double page pagination modes.
     */
    PAGE_SEPARATORS("metsEditor.pageSeparators", String.class, "\" \"", new ArrayList<>()),

    /*
     * backup of metadata configuration
     */

    /**
     * Backup of metadata configuration. Integer.
     */
    NUMBER_OF_META_BACKUPS("numberOfMetaBackups", null, null, new ArrayList<>()),

    /*
     * Metadata enrichment
     */

    /**
     * Set to true to enable the feature of automatic meta data inheritance and
     * enrichment. If this is enabled, all meta data elements from a higher level of
     * the logical document structure are automatically inherited and lower levels
     * are enriched with them upon process creation, given they have the same meta
     * data type addable. Boolean, defaults to false.
     */
    USE_METADATA_ENRICHMENT("useMetadataEnrichment", null, null, new ArrayList<>()),

    /*
     * Data copy rules
     */

    /**
     * Data copy rules may be used to copy Kitodo internal data and metadata on
     * catalogue query.
     */
    COPY_DATA_ON_CATALOGUE_QUERY("copyData.onCatalogueQuery", null, null, new ArrayList<>()),

    /**
     * Data copy rules may be used to copy Kitodo internal data and metadata on DMS
     * export.
     */
    COPY_DATA_ON_EXPORT("copyData.onExport", null, null, new ArrayList<>()),

    /*
     * Metadata validation
     */

    /**
     * Perform basic metadata validation or not. Boolean, defaults to {@code false}.
     */
    USE_META_DATA_VALIDATION("useMetadatenvalidierung", Boolean.TYPE, false, new ArrayList<>()),

    /**
     * Validation of process title via regular expression.
     */
    VALIDATE_PROCESS_TITLE_REGEX("validateProzessTitelRegex", String.class, "[\\w-]*", new ArrayList<>()),

    /**
     * Validation of the identifier via regular expression.
     */
    VALIDATE_IDENTIFIER_REGEX("validateIdentifierRegex", String.class, "[\\w|-]", new ArrayList<>()),

    /*
     * AUTOMATION
     *
     * Mass process generation
     */

    /**
     * Boolean, defaults to {@code false}.
     */
    MASS_IMPORT_ALLOWED("massImportAllowed", Boolean.TYPE, false, new ArrayList<>()),

    /**
     * Boolean, defaults to {@code true}.
     */
    MASS_IMPORT_UNIQUE_TITLE("MassImportUniqueTitle", Boolean.TYPE, true, new ArrayList<>()),

    /**
     * Colours used to represent the issues in the calendar editor.
     */
    ISSUE_COLOURS("issue.colours", String.class,
            "#CC0000;#0000AA;#33FF00;#FF9900;#5555FF;#006600;#AAAAFF;#000055;#0000FF;#FFFF00;#000000",
            new ArrayList<>()),

    /**
     * Number of pages per process below which the features in the granularity
     * dialog shall be locked. Long.
     */
    MINIMAL_NUMBER_OF_PAGES("numberOfPages.minimum", null, null, new ArrayList<>()),

    /*
     * Batch processing
     */

    /**
     * Limits the number of batches showing on the page “Batches”. Defaults to -1
     * which disables this functionality. If set, only the limited number of batches
     * will be shown, the other batches will be present but hidden and thus cannot
     * be modified and not even be deleted. Integer.
     */
    BATCH_DISPLAY_LIMIT("batchMaxSize", null, null, new ArrayList<>()),

    /**
     * Turn on or off whether each assignment of processes to or removal from
     * batches shall result in rewriting each processes' wiki field in order to
     * leave a note there. Enabling this function may slow down operations in the
     * batches dialogue. Boolean, defaults to {@code false}.
     */
    BATCHES_LOG_CHANGES("batches.logChangesToWikiField", Boolean.TYPE, false, new ArrayList<>()),

    /*
     * content server for PDF generation
     */

    /**
     * Boolean, defaults to {@code false}.
     */
    PDF_AS_DOWNLOAD("pdfAsDownload", Boolean.TYPE, false, new ArrayList<>()),

    /**
     * If empty, internal content server will be used.
     */
    KITODO_CONTENT_SERVER_URL("kitodoContentServerUrl", null, null, new ArrayList<>()),

    /**
     * Timeout for content server requests via HTTP in ms. Integer, defaults to
     * 60000 (60 sec).
     */

    KITODO_CONTENT_SERVER_TIMEOUT("kitodoContentServerTimeOut", Integer.TYPE,
            (int) TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS), new ArrayList<>()),

    /*
     * Task manager
     */

    /**
     * Overrides the limit of tasks run in parallel. Integer, defaults to the number
     * of available cores.
     */
    TASK_MANAGER_AUTORUN_LIMIT("taskManager.autoRunLimit", null, null, new ArrayList<>()),

    /**
     * Sets the time interval between two inspections of the task list. Long,
     * defaults to 2000 ms.
     */
    TASK_MANAGER_INSPECTION_INTERVAL_MILLIS("taskManager.inspectionIntervalMillis", Long.TYPE,
            TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS), new ArrayList<>()),

    /**
     * Sets the maximum number of failed threads to keep around in RAM. Keep in mind
     * that zombie processes still occupy all their resources and aren't available
     * for garbage collection, so choose these values as restrictive as possible.
     * Integer, defaults to 10.
     */
    TASK_MANAGER_KEEP_FAILED("taskManager.keepThreads.failed.count", Integer.TYPE, 10, new ArrayList<>()),

    /**
     * Sets the maximum time to keep failed threads around in RAM. Keep in mind that
     * zombie processes still occupy all their resources and aren't available for
     * garbage collection, so choose these values as restrictive as possible.
     * Integer, defaults to 240 minutes.
     */
    TASK_MANAGER_KEEP_FAILED_MINS("taskManager.keepThreads.failed.minutes", Long.TYPE,
            TimeUnit.MINUTES.convert(4, TimeUnit.HOURS), new ArrayList<>()),

    /**
     * Sets the maximum number of successfully finished threads to keep around in
     * RAM. Defaults to 3. Keep in mind that zombie processes still occupy all their
     * resources and aren't available for garbage collection, so choose these values
     * as restrictive as possible.
     */
    TASK_MANAGER_KEEP_SUCCESSFUL("taskManager.keepThreads.successful.count", Integer.TYPE, 3, new ArrayList<>()),

    /**
     * Sets the maximum time to keep successfully finished threads around in RAM.
     * Defaults to 20 minutes. Keep in mind that zombie processes still occupy all
     * their resources and aren't available for garbage collection, so choose these
     * values as restrictive as possible.
     */
    TASK_MANAGER_KEEP_SUCCESSFUL_MINS("taskManager.keepThreads.successful.minutes", Long.TYPE,
            TimeUnit.MINUTES.convert(20, TimeUnit.MINUTES), new ArrayList<>()),

    /**
     * Sets whether or not to show an option to "add a sample task" in the task
     * manager. This isif for anything at all—useful for debugging or demonstration
     * purposes only. Boolean, defaults to {@code false}.
     */
    TASK_MANAGER_SHOW_SAMPLE_TASK("taskManager.showSampleTask", Boolean.TYPE, false, new ArrayList<>()),

    /*
     * Export to presentation module
     */

    /**
     * If you set this to true the exports will be done asynchronously (in the
     * background). This requires that the automatic export was set up in the
     * project settings. Boolean, defaults to {@code false}.
     */
    ASYNCHRONOUS_AUTOMATIC_EXPORT("asynchronousAutomaticExport", Boolean.TYPE, false, new ArrayList<>()),

    /**
     * Whether during an export to the DMS the images will be copied. Boolean,
     * defaults to {@code true}.
     */
    EXPORT_WITH_IMAGES("automaticExportWithImages", Boolean.TYPE, true, new ArrayList<>()),

    /**
     * Boolean, defaults to {@code true}.
     */
    AUTOMATIC_EXPORT_WITH_OCR("automaticExportWithOcr", Boolean.TYPE, true, new ArrayList<>()),

    /**
     * Boolean, defaults to {@code true}.
     */
    EXPORT_VALIDATE_IMAGES("ExportValidateImages", Boolean.TYPE, true, new ArrayList<>()),

    /**
     * Boolean, defaults to {@code false}.
     */
    EXPORT_WITHOUT_TIME_LIMIT("exportWithoutTimeLimit", Boolean.TYPE, false, new ArrayList<>()),

    /*
     * REMOTE SERVICES
     *
     * LDAP Configuration
     */

    /**
     * Boolean, defaults to {@code true}.
     */
    LDAP_USE("ldap_use", Boolean.TYPE, true, new ArrayList<>()),

    LDAP_ATTRIBUTE_TO_TEST("ldap_AttributeToTest", null, null, new ArrayList<>()),

    LDAP_VALUE_OF_ATTRIBUTE("ldap_ValueOfAttribute", null, null, new ArrayList<>()),

    /**
     * Boolean, defaults to {@code true}.
     */
    LDAP_USE_LOCAL_DIRECTORY("useLocalDirectory", Boolean.TYPE, true, new ArrayList<>()),

    /**
     * Boolean, defaults to {@code false}.
     */
    LDAP_USE_TLS("ldap_useTLS", Boolean.TYPE, false, new ArrayList<>()),

    LDAP_USE_SIMPLE_AUTH("useSimpleAuthentification", null, null, new ArrayList<>()),

    /*
     * Authority control configuration
     */

    /**
     * Which authority identifier to use for a given URI prefix.
     *
     * <p>
     * Example: authority.http\://d-nb.info/gnd/.id=gnd
     * </p>
     */
    AUTHORITY_ID_FROM_URI("authority.{0}.id", null, null, new ArrayList<>()),

    /**
     * Content to put in the URI field when adding a new metadata element of type
     * person. This should usually be your preferred norm data file’s URI prefix as
     * to the user doesn’t have to enter it over and over again.
     *
     * <p>
     * Example: authority.default=http\://d-nb.info/gnd/
     * </p>
     */
    AUTHORITY_DEFAULT("authority.default", null, null, new ArrayList<>()),

    /*
     * FUNCTIONAL EXTENSIONS
     *
     * OCR service access
     */

    /**
     * Boolean, defaults to {@code false}.
     */
    SHOW_OCR_BUTTON("showOcrButton", Boolean.TYPE, false, new ArrayList<>()),

    /**
     * Base path to OCR, without parameters.
     */
    OCR_URL("ocrUrl", null, null, new ArrayList<>()),

    /*
     * ActiveMQ web services
     */

    ACTIVE_MQ_HOST_URL("activeMQ.hostURL", null, null, new ArrayList<>()),

    ACTIVE_MQ_CREATE_NEW_PROCESSES_QUEUE("activeMQ.createNewProcess.queue", null, null, new ArrayList<>()),

    ACTIVE_MQ_FINALIZE_STEP_QUEUE("activeMQ.finaliseStep.queue", null, null, new ArrayList<>()),

    ACTIVE_MQ_RESULTS_TOPIC("activeMQ.results.topic", null, null, new ArrayList<>()),

    /**
     * Long, value in milliseconds.
     */
    ACTIVE_MQ_RESULTS_TTL("activeMQ.results.timeToLive", Long.TYPE, TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS),
            new ArrayList<>()),

    /*
     * Elasticsearch properties
     */

    ELASTICSEARCH_BATCH("elasticsearch.batch", Integer.TYPE, 1000, new ArrayList<>());

    private String name;
    private Class type;
    private Object defaultValue;
    private List<Object> possibleValues;

    /**
     * Private constructor to hide the implicit public one.
     * 
     * @param name
     *            of parameter
     */
    ParameterCore(String name, Class type, Object defaultValue, List<Object> possibleValues) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.possibleValues = possibleValues;
    }

    /**
     * Get type.
     *
     * @return value of type
     */
    public Class getType() {
        return type;
    }

    /**
     * Get default value for parameter.
     *
     * @return value of defaultValue
     */
    public Object getDefaultValue() {
        if (Objects.nonNull(defaultValue)) {
            return defaultValue;
        }
        throw new ConfigParameterException(this.name);
    }

    /**
     * Get possible values.
     *
     * @return value of possibleValues
     */
    public List<Object> getPossibleValues() {
        return possibleValues;
    }

    @Override
    public java.lang.String toString() {
        return this.name;
    }
}
