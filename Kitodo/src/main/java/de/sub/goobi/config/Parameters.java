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

package de.sub.goobi.config;

import java.util.concurrent.TimeUnit;

/**
 * These constants define configuration parameters usable in the configuration
 * file. This file reflects the order of the global
 * {@code kitodo_config.properties} file (as far as possible), and vice versa.
 */
public class Parameters {

    // =============================================================================
    // FILE AND DIRECTORY MANAGEMENT
    // =============================================================================
    // -----------------------------------
    // Directories
    // -----------------------------------

    // DIR_XML_CONFIG = "KonfigurationVerzeichnis" → org.kitodo.config.Config

    /**
     * Absolute path to the directory that the rule set definition files will be
     * read from. It must be terminated by a directory separator ("/").
     */
    public static final String DIR_RULESETS = "RegelsaetzeVerzeichnis";

    /**
     * Absolute path to the directory that XSLT files are stored in which are
     * used to transform the "XML log" (as visible from the XML button in the
     * processes list) to a downloadable PDF docket which can be enclosed with
     * the physical binding units to digitise. The path must be terminated by a
     * directory separator ("/").
     */
    public static final String DIR_XSLT = "xsltFolder";

    // DIR_PROCESSES = "MetadatenVerzeichnis" → org.kitodo.config.Config

    /**
     * Absolute path to the base directory of the users' home directories,
     * terminated by a directory separator ("/"). If a user accepts a task to
     * work on which will require him or her to have access permission to the
     * data of a process, a symbolic link to the process directory in question
     * will be created in his or her home directory that will be removed again
     * after finishing the task. Note: If LDAP is used, the users' home dirs
     * will instead be read from LDAP
     */
    public static final String DIR_USERS = "dir_Users";

    /**
     * Absolute path to a folder the application can temporarily create files
     * in, terminated by a directory separator ("/").
     */
    public static final String DIR_TEMP = "tempfolder";

    /**
     * Path to directory in which BPMN diagrams are stored.
     */
    public static final String DIR_DIAGRAMS = "diagramsFolder";

    // DIR_MODULES = "moduleFolder" → org.kitodo.config.Config

    /**
     * Points to a folder on the file system that contains <b>legacy</b>
     * Production plug-in jars. In the folder, there must be subfolders named as
     * defined in enum PluginType (currently: “import” and “opac”) in which the
     * plug-in jars must be stored.
     *
     * <p>
     * Must be terminated by the file separator.
     */
    public static final String DIR_PLUGINS = "pluginFolder";

    /**
     * Points to a folder on the file system that <b>legacy</b> plug-ins may use
     * to write temporary files.
     */
    public static final String DIR_PLUGINS_TEMP = "debugFolder";

    // -----------------------------------
    // Directory management
    // -----------------------------------

    /**
     * Boolean, defaults to {@code false}.
     */
    public static final String CREATE_ORIG_FOLDER_IF_NOT_EXISTS = "createOrigFolderIfNotExists";

    // "createSourceFolder" → org.kitodo.filemanagement.FileManagement

    /**
     * Prefix of image directory name created on process creation.
     */
    public static final String DIRECTORY_PREFIX = "DIRECTORY_PREFIX";

    /**
     * Directory suffix for created image directory on process creation.
     */
    public static final String DIRECTORY_SUFFIX = "DIRECTORY_SUFFIX";

    /**
     * Boolean, defaults to {@code false}.
     */
    public static final String IMPORT_USE_OLD_CONFIGURATION = "importUseOldConfiguration";

    /**
     * Creation and export of process sub-directories, e.g.
     * {@code images/(processtitle)_tif&ocr/(processtitle)_pdf}.
     * {@code (processtitle)} is a placeholder for the process title If you
     * comment in the parameter processDirs without a value, the result is that
     * the whole process directory will be exported and no directory well be
     * created. If you leave the parameter commented out, the whole
     * functionality is disabled. Using the {@code processDirs} parameter is
     * always an addition to the existing folder creating and exporting
     * functions of Kitodo.Production.
     */
    public static final String PROCESS_DIRS = "processDirs";

    /**
     * Set if master images folder {@code orig_} should be used at all. Boolean,
     * defaults to {@code true}.
     */
    public static final String USE_ORIG_FOLDER = "useOrigFolder";

    // -----------------------------------
    // Directory and symbolic link management
    // -----------------------------------

    /**
     * Script to create the user's home directory when adding a new user.
     */
    public static final String SCRIPT_CREATE_DIR_USER_HOME = "script_createDirUserHome";

    /**
     * Script to create the directory for a new process.
     */
    public static final String SCRIPT_CREATE_DIR_META = "script_createDirMeta";

    /**
     * Script to create a symbolic link in the user home direcory and set
     * permissions for the user.
     */
    public static final String SCRIPT_CREATE_SYMLINK = "script_createSymLink";

    /**
     * Script to remove the symbolic link from the user home directory.
     */
    public static final String SCRIPT_DELETE_SYMLINK = "script_deleteSymLink";

    // -----------------------------------
    // Runnotes
    // -----------------------------------

    /**
     * Filename of the XSLT file for transforming old metadata files which need
     * to be in the xslt folder above.
     */
    public static final String XSLT_FILENAME_METADATA_TRANSFORMATION = "xsltFilenameMetadataTransformation";

    // -----------------------------------
    // Images
    // -----------------------------------

    /**
     * Prefix for image names as regex. Default is 8 digits \\d{8} and gets
     * validated.
     */
    public static final String IMAGE_PREFIX = "ImagePrefix";

    /**
     * Sorting of images.
     */
    public static final String IMAGE_SORTING = "ImageSorting";

    /**
     * Sorting of images: 1 is lesser then 002, compares the number of image
     * names, characters other than digits are not supported
     */
    public static final String IMAGE_SORTING_VALUE_NUMBER = "number";

    /**
     * Sorting of images: 1 is greater then 002, compares character by character
     * of image names, all characters are supported.
     */
    public static final String IMAGE_SORTING_VALUE_ALPHANUMERIC = "alphanumeric";

    /**
     * Defaults to {@code fertig/}.
     */
    public static final String DONE_DIRECTORY_NAME = "doneDirectoryName";

    // =============================================================================
    // VISUAL APPEARANCE
    // =============================================================================
    // -----------------------------------
    // Internationalization
    // -----------------------------------

    /**
     * Absolute path to the directory that the resource bundle files are stored
     * in, terminated by a directory separator ("/").
     *
     * <p>
     * Note: If this directory DOESN'T EXIST, the internal resource bundles will
     * be used. If this directory exists BUT DOES NOT CONTAIN suitable
     * resources, the screens will not work as expected.
     */
    public static final String LOCAL_MESSAGES = "localMessages";

    /**
     * Start-up language: If not set, Kitodo.Production will start up with the
     * language best matching the user's Accept-Languages HTTP Request header.
     * You can override this behaviour by setting a default language here.
     */
    public static final String LANGUAGE_FORCE_DEFAULT = "language.force-default";

    /**
     * If no Accept-Language Http Request header is present, use the following
     * language.
     */
    public static final String LANGUAGE_DEFAULT = "language.default";

    // -----------------------------------
    // Data protection
    // -----------------------------------

    /**
     * The General Data Protection Regulation or local law might require to set
     * this value to true. anonymized statistics, displaying user on steps, etc.
     * Boolean, defaults to {@code false}.
     */
    public static final String ANONYMIZE = "anonymize";

    /**
     * Enable / disable search for steps done by user. Boolean, defaults to
     * {@code false}.
     */
    public static final String WITH_USER_STEP_DONE_SEARCH = "withUserStepDoneSearch";

    // -----------------------------------
    // Error page
    // -----------------------------------

    /**
     * Page the user will be directed to continue.
     */
    public static final String ERR_LINK_TO_PAGE = "err_linkToPage";

    // =============================================================================
    // METADATA PROCESSING
    // =============================================================================
    // -----------------------------------
    // Catalogue search
    // -----------------------------------

    /**
     * Number of hits to show per page on the hitlist when multiple hits were
     * found on a catalogue search. Integer, defaults to 12.
     */
    public static final String HITLIST_PAGE_SIZE = "catalogue.hitlist.pageSize";

    /**
     * Indicates the maximum duration an interaction with a library catalogue
     * may take. Milliseconds, defaults to 30 minutes.
     */
    public static final String CATALOGUE_TIMEOUT = "catalogue.timeout";

    // -----------------------------------
    // Metadata editor behavior
    // -----------------------------------
    /**
     * Long, value in milliseconds. Defaults to 180000 (30 minutes).
     */
    public static final String METS_EDITOR_LOCKING_TIME = "MetsEditorLockingTime";

    /**
     * Use special image folder for METS editor if exists (define suffix here).
     */
    public static final String METS_EDITOR_DEFAULT_SUFFIX = "MetsEditorDefaultSuffix";

    /**
     * Enables or disables automatic pagination changes in the meta-data editor.
     * If false, pagination must be updated manually by clicking the link “Read
     * in pagination from images”. Boolean, defaults to {@code true}.
     */
    public static final String WITH_AUTOMATIC_PAGINATION = "MetsEditorWithAutomaticPagination";

    /**
     * Use special pagination type for automatic default pagination.
     */
    public static final String METS_EDITOR_DEFAULT_PAGINATION = "MetsEditorDefaultPagination";
    public static final String METS_EDITOR_DEFAULT_SUFFIX_VALUE_ARABIC = "arabic";
    public static final String METS_EDITOR_DEFAULT_SUFFIX_VALUE_ROMAN = "roman";
    public static final String METS_EDITOR_DEFAULT_SUFFIX_VALUE_UNCOUNTED = "uncounted";

    /**
     * Use a maximum of characters to display titles in the left part of mets
     * editor. Integer, the default value is 0 (everything is displayed).
     */
    public static final String METS_EDITOR_MAX_TITLE_LENGTH = "MetsEditorMaxTitleLength";

    /**
     * Initialize all sub elements in METS editor to assign default values.
     * Boolean, defaults to {@code true}.
     */
    public static final String METS_EDITOR_ENABLE_DEFAULT_INITIALISATION = "MetsEditorEnableDefaultInitialisation";

    /**
     * Display the file manipulation dialog within the METS editor.
     */
    public static final String METS_EDITOR_DISPLAY_FILE_MANIPULATION = "MetsEditorDisplayFileManipulation";

    /**
     * Comma-separated list of Strings which may be enclosed in double quotes.
     * Separators available for double page pagination modes.
     */
    public static final String PAGE_SEPARATORS = "pageSeparators";

    // -----------------------------------
    // backup of metadata configuration
    // -----------------------------------

    /**
     * Backup of metadata configuration. Integer.
     */
    public static final String NUMBER_OF_META_BACKUPS = "numberOfMetaBackups";

    // -----------------------------------
    // Metadata enrichment
    // -----------------------------------

    /**
     * Set to true to enable the feature of automatic meta data inheritance and
     * enrichment. If this is enabled, all meta data elements from a higher
     * level of the logical document structure are automatically inherited and
     * lower levels are enriched with them upon process creation, given they
     * have the same meta data type addable. Boolean, defaults to false.
     */
    public static final String USE_METADATA_ENRICHMENT = "useMetadataEnrichment";

    // -----------------------------------
    // Data copy rules
    // -----------------------------------

    /**
     * Data copy rules may be used to copy Kitodo internal data and metadata on
     * catalogue query.
     */
    public static final String COPY_DATA_ON_CATALOGUE_QUERY = "copyData.onCatalogueQuery";

    /**
     * Data copy rules may be used to copy Kitodo internal data and metadata on
     * DMS export.
     */
    public static final String COPY_DATA_ON_EXPORT = "copyData.onExport";

    // -----------------------------------
    // Metadata validation
    // -----------------------------------

    /**
     * Perform basic metadata validation or not. Boolean, defaults to
     * {@code false}.
     */
    public static final String USE_META_DATA_VALIDATION = "useMetadatenvalidierung";

    /**
     * Validation of process title via regular expression.
     */
    public static final String VALIDATE_PROCESS_TITLE_REGEX = "validateProzessTitelRegex";

    /**
     * Validation of the identifier via regular expression.
     */
    public static final String VALIDATE_IDENTIFIER_REGEX = "validateIdentifierRegex";

    // =============================================================================
    // AUTOMATION
    // =============================================================================
    // -----------------------------------
    // Mass process generation
    // -----------------------------------

    /**
     * Boolean, defaults to {@code false}.
     */
    public static final String MASS_IMPORT_ALLOWED = "massImportAllowed";

    /**
     * Boolean, defaults to {@code true}.
     */
    public static final String MASS_IMPORT_UNIQUE_TITLE = "MassImportUniqueTitle";

    /**
     * Colours used to represent the issues in the calendar editor.
     */
    public static final String ISSUE_COLOURS = "issue.colours";

    /**
     * Number of pages per process below which the features in the granualarity
     * dialog shall be locked. Long.
     */
    public static final String MINIMAL_NUMBER_OF_PAGES = "numberOfPages.minimum";

    // -----------------------------------
    // Batch processing
    // -----------------------------------

    /**
     * Limits the number of batches showing on the page “Batches”. Defaults to
     * -1 which disables this functionality. If set, only the limited number of
     * batches will be shown, the other batches will be present but hidden and
     * thus cannot be modified and not even be deleted. Integer.
     */
    public static final String BATCH_DISPLAY_LIMIT = "batchMaxSize";

    /**
     * Turn on or off whether each assignment of processes to or removal from
     * batches shall result in rewriting each processes' wiki field in order to
     * leave a note there. Enabling this function may slow down operations in
     * the batches dialogue. Boolean, defaults to {@code false}.
     */
    public static final String BATCHES_LOG_CHANGES = "batches.logChangesToWikiField";

    // -----------------------------------
    // content server for PDF generation
    // -----------------------------------

    /**
     * Boolean, defaults to {@code false}.
     */
    public static final String PDF_AS_DOWNLOAD = "pdfAsDownload";

    /**
     * If empty, internal content server will be used.
     */
    public static final String KITODO_CONTENT_SERVER_URL = "kitodoContentServerUrl";

    /**
     * Timeout for content server requests via HTTP in ms. Integer, defaults to
     * 60000 (60 sec).
     */

    public static final String KITODO_CONTENT_SERVER_TIMEOUT = "kitodoContentServerTimeOut";

    // -----------------------------------
    // Task manager
    // -----------------------------------

    /**
     * Overrides the limit of tasks run in parallel. Integer, defaults to the
     * number of available cores.
     */
    public static final String TASK_MANAGER_AUTORUN_LIMIT = "taskManager.autoRunLimit";

    /**
     * Sets the time interval between two inspections of the task list. Long,
     * defaults to 2000 ms.
     */
    public static final String TASK_MANAGER_INSPECTION_INTERVAL_MILLIS = "taskManager.inspectionIntervalMillis";

    /**
     * Sets the maximum number of failed threads to keep around in RAM. Keep in
     * mind that zombie processes still occupy all their ressources and aren't
     * available for garbage collection, so choose these values as restrictive
     * as possible. Integer, defaults to 10.
     */
    public static final String TASK_MANAGER_KEEP_FAILED = "taskManager.keepThreads.failed.count";

    /**
     * Sets the maximum time to keep failed threads around in RAM. Keep in mind
     * that zombie processes still occupy all their ressources and aren't
     * available for garbage collection, so choose these values as restrictive
     * as possible. Integer, defaults to 240 minutes.
     */
    public static final String TASK_MANAGER_KEEP_FAILED_MINS = "taskManager.keepThreads.failed.minutes";

    /**
     * Sets the maximum number of successfully finished threads to keep around
     * in RAM. Defaults to 3. Keep in mind that zombie processes still occupy
     * all their ressources and aren't available for garbage collection, so
     * choose these values as restrictive as possible.
     */
    public static final String TASK_MANAGER_KEEP_SUCCESSFUL = "taskManager.keepThreads.successful.count";

    /**
     * Sets the maximum time to keep successfully finished threads around in
     * RAM. Defaults to 20 minutes. Keep in mind that zombie processes still
     * occupy all their resources and aren't available for garbage collection,
     * so choose these values as restrictive as possible.
     */
    public static final String TASK_MANAGER_KEEP_SUCCESSFUL_MINS = "taskManager.keepThreads.successful.minutes";

    /**
     * Sets whether or not to show an option to "add a sample task" in the task
     * manager. This is---if for anything at all—useful for debugging or
     * demonstration purposes only. Boolean, defaults to {@code false}.
     */
    public static final String TASK_MANAGER_SHOW_SAMPLE_TASK = "taskManager.showSampleTask";

    // -----------------------------------
    // Export to presentation module
    // -----------------------------------

    /**
     * If you set this to true the exports will be done asynchronously (in the
     * background). This requires that the automatic export was set up in the
     * project settings. Boolean, defaults to {@code false}.
     */
    public static final String ASYNCHRONOUS_AUTOMATIC_EXPORT = "asynchronousAutomaticExport";

    /**
     * Whether during an export to the DMS the images will be copied. Boolean,
     * defaults to {@code true}.
     */
    public static final String EXPORT_WITH_IMAGES = "automaticExportWithImages";

    /**
     * Boolean, defaults to {@code true}.
     */
    public static final String AUTOMATIC_EXPORT_WITH_OCR = "automaticExportWithOcr";

    /**
     * Boolean, defaults to {@code true}.
     */
    public static final String EXPORT_VALIDATE_IMAGES = "ExportValidateImages";

    /**
     * Boolean, defaults to {@code false}.
     */
    public static final String EXPORT_WITHOUT_TIME_LIMIT = "exportWithoutTimeLimit";

    // =============================================================================
    // REMOTE SERVICES
    // =============================================================================
    // -----------------------------------
    // LDAP Configuration
    // -----------------------------------

    /**
     * Boolean, defaults to {@code true}.
     */
    public static final String LDAP_USE = "ldap_use";

    public static final String LDAP_ATTRIBUTE_TO_TEST = "ldap_AttributeToTest";

    public static final String LDAP_VALUE_OF_ATTRIBUTE = "ldap_ValueOfAttribute";

    /**
     * Boolean, defaults to {@code false}.
     */
    public static final String LDAP_USE_LOCAL_DIRECTORY = "useLocalDirectory";

    /**
     * Boolean, defaults to {@code false}.
     */
    public static final String LDAP_USE_TLS = "ldap_useTLS";

    // -----------------------------------
    // Authority control configuration
    // -----------------------------------

    /**
     * Which authority identifier to use for a given URI prefix.
     *
     * <p>
     * Example: authority.http\://d-nb.info/gnd/.id=gnd
     * </p>
     */
    public static final String AUTHORITY_ID_FROM_URI = "authority.{0}.id";

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

    // =============================================================================
    // INTERACTIVE ERROR MANAGEMENT
    // =============================================================================

    /**
     * Boolean, defaults to {@code false}.
     */
    public static final String ERR_USER_HANDLING = "err_userHandling";

    /**
     * Use this to turn the email feature on or off. Boolean, defaults to
     * {@code false}.
     */
    public static final String ERR_EMAIL_ENABLED = "err_emailEnabled";

    /**
     * An indefinite number of e-mail addresses can be entered here. Create one
     * enumerated configuration entry line per address, like:
     * {@code err_emailAddress1=(...)}, {@code err_emailAddress2=(...)},
     * {@code err_emailAddress3=(...)}, ...
     */
    public static final String ERR_EMAIL_ADDRESS = "err_emailAddress";

    // =============================================================================
    // FUNCTIONAL EXTENSIONS
    // =============================================================================
    // -----------------------------------
    // OCR service access
    // -----------------------------------

    /**
     * Boolean, defaults to {@code false}.
     */
    public static final String SHOW_OCR_BUTTON = "showOcrButton";

    /**
     * Base path to OCR, without parameters.
     */
    public static final String OCR_URL = "ocrUrl";

    // -----------------------------------
    // ActiveMQ web services
    // -----------------------------------

    public static final String ACTIVE_MQ_HOST_URL = "activeMQ.hostURL";

    public static final String ACTIVE_MQ_CREATE_NEW_PROCESSES_QUEUE = "activeMQ.createNewProcess.queue";

    public static final String ACTIVE_MQ_FINALIZE_STEP_QUEUE = "activeMQ.finaliseStep.queue";

    public static final String ACTIVE_MQ_RESULTS_TOPIC = "activeMQ.results.topic";

    /**
     * Long, value in milliseconds.
     */
    public static final String ACTIVE_MQ_RESULTS_TTL = "activeMQ.results.timeToLive";

    // -----------------------------------
    // Elasticsearch properties
    // -----------------------------------

    public static final String ELASTICSEARCH_BATCH = "elasticsearch.batch";

    /**
     * Hard-coded default values.
     */
    public static class DefaultValues {
        public static final long ACTIVE_MQ_RESULTS_TTL = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
        public static final long CATALOGUE_TIMEOUT = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);
        public static final String DIRECTORY_PREFIX = "orig";
        public static final String DIRECTORY_SUFFIX = "tif";
        public static final String DONE_DIRECTORY_NAME = "fertig/";
        public static final int ELASTICSEARCH_BATCH = 1000;
        public static final String ERR_LINK_TO_PAGE = "./start.jsf";
        public static final int HITLIST_PAGE_SIZE = 10;
        public static final int KITODO_CONTENT_SERVER_TIMEOUT = (int) TimeUnit.MILLISECONDS.convert(60,
            TimeUnit.SECONDS);
        public static final String IMAGE_PREFIX = "\\d{8}";
        public static final String IMAGE_SORTING = IMAGE_SORTING_VALUE_NUMBER;
        public static final String ISSUE_COLOURS = "#CC0000;#0000AA;#33FF00;#FF9900;#5555FF;#006600;#AAAAFF;#000055;#0000FF;#FFFF00;#000000";
        public static final String LANGUAGE_DEFAULT = "de";
        public static final String LANGUAGE_FORCE_DEFAULT = "de";
        public static final String LOCAL_MESSAGES = "/usr/local/kitodo/messages/";
        public static final long METS_EDITOR_LOCKING_TIME = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);
        public static final String PAGE_SEPARATORS = "\" \"";
        public static final long TASK_MANAGER_INSPECTION_INTERVAL_MILLIS = TimeUnit.MILLISECONDS.convert(2,
            TimeUnit.SECONDS);
        public static final int TASK_MANAGER_KEEP_FAILED = 10;
        public static final long TASK_MANAGER_KEEP_FAILED_MINS = TimeUnit.MINUTES.convert(4, TimeUnit.HOURS);
        public static final int TASK_MANAGER_KEEP_SUCCESSFUL = 3;
        public static final long TASK_MANAGER_KEEP_SUCCESSFUL_MINS = TimeUnit.MINUTES.convert(20, TimeUnit.MINUTES);
        public static final String TEMPFOLDER = "/usr/local/kitodo/temp/";
        public static final String VALIDATE_IDENTIFIER_REGEX = "[\\w|-]";
        public static final String VALIDATE_PROCESS_TITLE_REGEX = "[\\w-]*";
    }

    /**
     * Private constructor to hide the implicit public one.
     */
    private Parameters() {

    }
}
