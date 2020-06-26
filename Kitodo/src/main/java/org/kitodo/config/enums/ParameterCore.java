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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.kitodo.config.beans.Parameter;
import org.kitodo.config.beans.UndefinedParameter;

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
     * directory.config in kitodo_config.properties, but there is no constant for it
     * here. This comment is to explain where the constant is if someone compares
     * the two files in the future:
     */

    DIR_XML_CONFIG(new Parameter<UndefinedParameter>("directory.config")),

    /**
     * Absolute path to the directory that the rule set definition files will be
     * read from. It must be terminated by a directory separator ("/").
     */
    DIR_RULESETS(new Parameter<UndefinedParameter>("directory.rulesets")),

    /**
     * Absolute path to the directory that XSLT files are stored in which are used
     * to transform the "XML log" (as visible from the XML button in the processes
     * list) to a downloadable PDF docket which can be enclosed with the physical
     * binding units to digitise. The path must be terminated by a directory
     * separator ("/").
     */
    DIR_XSLT(new Parameter<UndefinedParameter>("directory.xslt")),

    /*
     * Parameters.java has been sorted to corresponding to kitodo_config.properties,
     * including the section headers as you see. However there is an entry
     * directory.metadata in kitodo_config.properties, but there is no constant for
     * it here. This comment is to explain where the constant is if someone compares
     * the two files in the future:
     */

    DIR_PROCESSES(new Parameter<UndefinedParameter>("directory.metadata")),

    /**
     * Absolute path to the base directory of the users' home directories,
     * terminated by a directory separator ("/"). If a user accepts a task to work
     * on which will require him or her to have access permission to the data of a
     * process, a symbolic link to the process directory in question will be created
     * in his or her home directory that will be removed again after finishing the
     * task. Note: If LDAP is used, the users' home dirs will instead be read from
     * LDAP
     */
    DIR_USERS(new Parameter<UndefinedParameter>("directory.users")),

    /**
     * Absolute path to a folder the application can temporarily create files in,
     * terminated by a directory separator ("/").
     */
    DIR_TEMP(new Parameter<>("directory.temp", "/usr/local/kitodo/temp/")),

    /**
     * Path to directory in which BPMN diagrams are stored.
     */
    DIR_DIAGRAMS(new Parameter<UndefinedParameter>("directory.diagrams")),

    /*
     * Parameters.java has been sorted to corresponding to kitodo_config.properties,
     * including the section headers as you see. However there is an entry
     * directory.modules in kitodo_config.properties, but there is no constant for
     * it here. This comment is to explain where the constant is if someone compares
     * the two files in the future:
     */

    DIR_MODULES(new Parameter<UndefinedParameter>("directory.modules")),

    /**
     * Points to a folder on the file system that contains <b>legacy</b> Production
     * plug-in jars. In the folder, there must be subfolders named as defined in
     * enum PluginType (currently: “import” and “opac”) in which the plug-in jars
     * must be stored.
     *
     * <p>
     * Must be terminated by the file separator.
     */
    DIR_PLUGINS(new Parameter<UndefinedParameter>("directory.plugins")),

    /**
     * Points to a folder on the file system that <b>legacy</b> plug-ins may use to
     * write temporary files.
     */
    DIR_PLUGINS_TEMP(new Parameter<UndefinedParameter>("directory.debug")),

    /*
     * Directory management
     */

    /**
     * Prefix of image directory name created on process creation.
     */
    DIRECTORY_PREFIX(new Parameter<>("DIRECTORY_PREFIX", "orig")),

    /**
     * Directory suffix for created image directory on process creation.
     */
    DIRECTORY_SUFFIX(new Parameter<>("DIRECTORY_SUFFIX", "tif")),

    /**
     * Boolean, defaults to {@code false}.
     */
    IMPORT_USE_OLD_CONFIGURATION(new Parameter<>("importUseOldConfiguration", false)),

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
    PROCESS_DIRS(new Parameter<UndefinedParameter>("processDirs")),

    /**
     * Set if master images folder {@code orig_} should be used at all. Boolean,
     * defaults to {@code true}.
     */
    USE_ORIG_FOLDER(new Parameter<>("useOrigFolder", true)),

    /*
     * Directory and symbolic link management
     */

    /**
     * Script to create the user's home directory when adding a new user.
     */
    SCRIPT_CREATE_DIR_USER_HOME(new Parameter<UndefinedParameter>("script_createDirUserHome")),

    /**
     * Script to create the directory for a new process.
     */
    SCRIPT_CREATE_DIR_META(new Parameter<UndefinedParameter>("script_createDirMeta")),

    /**
     * Script to create a symbolic link in the user home directory and set
     * permissions for the user.
     */
    SCRIPT_CREATE_SYMLINK(new Parameter<UndefinedParameter>("script_createSymLink")),

    /**
     * Script to remove the symbolic link from the user home directory.
     */
    SCRIPT_DELETE_SYMLINK(new Parameter<UndefinedParameter>("script_deleteSymLink")),

    /**
     * Process property to use in the name of the smLink.
     */
    PROCESS_PROPERTY_SYMLINK_NAME(new Parameter<>("processProperty_symLinkName", "")),

    /*
     * Runnotes
     */

    /**
     * Filename of the XSLT file for transforming old metadata files which need to
     * be in the xslt folder above.
     */
    XSLT_FILENAME_METADATA_TRANSFORMATION(new Parameter<UndefinedParameter>("xsltFilenameMetadataTransformation")),

    /*
     * Images
     */

    /**
     * Prefix for image names as regex. Default is 8 digits \\d{8} and gets
     * validated.
     */
    IMAGE_PREFIX(new Parameter<>("image.prefix", "\\d{8}")),

    /**
     * Sorting of images.
     *
     * <p>
     * Numeric sorting of images. 1 is lesser then 002, compares the number of
     * image names, characters other than digits are not supported.
     *
     * <p>
     * Alphanumeric sorting of images. 1 is greater then 002, compares character
     * by character of image names, all characters are supported.
     */
    IMAGE_SORTING(new Parameter<>("image.sorting", "number", Arrays.asList("number", "alphanumeric"))),

    /**
     * Defaults to {@code fertig/}.
     */
    DONE_DIRECTORY_NAME(new Parameter<>("image.doneDirectoryName", "fertig/")),

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
    DIR_LOCAL_MESSAGES(new Parameter<>("directory.messages", "/usr/local/kitodo/messages/")),

    /**
     * Start-up language: If not set, Kitodo.Production will start up with the
     * language best matching the user's Accept-Languages HTTP Request header. You
     * can override this behavior by setting a default language here.
     */
    LANGUAGE_FORCE_DEFAULT(new Parameter<>("language.force-default", "de")),

    /**
     * If no Accept-Language Http Request header is present, use the following
     * language.
     */
    LANGUAGE_DEFAULT(new Parameter<>("language.default", "de")),

    METADATA_LANGUAGE_LIST(new Parameter<>("metadataLanguage.list", "Deutsch-de")),

    SHORTCUTS_DEFAULT(new Parameter<>("shortcuts.default", "{\"detailView\":\"Control Shift BracketRight\",\"help\":\"Shift Minus\",\"nextItem\":\"Control ArrowDown\",\"nextItemMulti\":\"Control Shift ArrowDown\",\"previousItem\":\"Control ArrowUp\",\"previousItemMulti\":\"Control Shift ArrowUp\",\"structuredView\":\"Control Shift Slash\"}")),

    /*
     * Data protection
     */

    /**
     * The General Data Protection Regulation or local law might require to set this
     * value to true. anonymized statistics, displaying user on steps, etc. Boolean,
     * defaults to {@code false}.
     */
    ANONYMIZE(new Parameter<>("anonymize", false)),

    /**
     * Enable / disable search for steps done by user. Boolean, defaults to
     * {@code false}.
     */
    WITH_USER_STEP_DONE_SEARCH(new Parameter<>("withUserStepDoneSearch", false)),

    /*
     * METADATA PROCESSING
     *
     * Catalogue search
     */

    /**
     * Number of hits to show per page on the hitlist when multiple hits were found
     * on a catalog search. Integer, defaults to 12.
     */
    HITLIST_PAGE_SIZE(new Parameter<>("catalogue.hitlist.pageSize", 12)),

    /**
     * Indicates the maximum duration an interaction with a library catalog may
     * take. Milliseconds, defaults to 30 minutes.
     */
    CATALOGUE_TIMEOUT(new Parameter<>("catalogue.timeout", TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES))),

    /*
     * Metadata editor behavior
     */

    /**
     * Use special image folder for METS editor if exists (define suffix here).
     */
    METS_EDITOR_DEFAULT_SUFFIX(new Parameter<UndefinedParameter>("metsEditor.defaultSuffix")),

    /**
     * Enables or disables automatic pagination changes in the metadata editor. If
     * false, pagination must be updated manually by clicking the link “Read in
     * pagination from images”. Boolean, defaults to {@code true}.
     */
    WITH_AUTOMATIC_PAGINATION(new Parameter<>("metsEditor.useAutomaticPagination", true)),

    /**
     * Use special pagination type for automatic default pagination.
     */
    METS_EDITOR_DEFAULT_PAGINATION(new Parameter<>("metsEditor.defaultPagination", "uncounted",
            Arrays.asList("arabic", "roman", "uncounted"))),

    /**
     * Use a maximum of characters to display titles in the left part of mets
     * editor. Integer, the default value is 0 (everything is displayed).
     */
    METS_EDITOR_MAX_TITLE_LENGTH(new Parameter<UndefinedParameter>("metsEditor.maxTitleLength")),

    /**
     * Initialize all sub elements in METS editor to assign default values. Boolean,
     * defaults to {@code true}.
     */
    METS_EDITOR_ENABLE_DEFAULT_INITIALISATION(new Parameter<>("metsEditor.enableDefaultInitialisation", true)),

    /**
     * Display the file manipulation dialog within the METS editor.
     */
    METS_EDITOR_DISPLAY_FILE_MANIPULATION(new Parameter<UndefinedParameter>("metsEditor.displayFileManipulation")),

    /**
     * Comma-separated list of Strings which may be enclosed in double quotes.
     * Separators available for double page pagination modes.
     */
    PAGE_SEPARATORS(new Parameter<>("metsEditor.pageSeparators", "\" \"")),

    /**
     # Priority list of metadata keys used to display title information in the metadata editors structure and gallery
     panels.
     */
    TITLE_KEYS(new Parameter<>("metsEditor.titleMetadata", "")),

    /*
     * backup of metadata configuration
     */

    /**
     * Backup of metadata configuration. Integer.
     */
    NUMBER_OF_META_BACKUPS(new Parameter<UndefinedParameter>("numberOfMetaBackups")),

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
    USE_METADATA_ENRICHMENT(new Parameter<UndefinedParameter>("useMetadataEnrichment")),

    /*
     * Data copy rules
     */

    /**
     * Data copy rules may be used to copy Kitodo internal data and metadata on
     * catalog query.
     */
    COPY_DATA_ON_CATALOGUE_QUERY(new Parameter<UndefinedParameter>("copyData.onCatalogueQuery")),

    /**
     * Data copy rules may be used to copy Kitodo internal data and metadata on DMS
     * export.
     */
    COPY_DATA_ON_EXPORT(new Parameter<UndefinedParameter>("copyData.onExport")),

    /*
     * Metadata validation
     */

    /**
     * Perform basic metadata validation or not. Boolean, defaults to {@code false}.
     */
    USE_META_DATA_VALIDATION(new Parameter<>("useMetadatenvalidierung", false)),

    /**
     * Validation of process title via regular expression.
     */
    VALIDATE_PROCESS_TITLE_REGEX(new Parameter<>("validateProzessTitelRegex", "[\\w-]*")),

    /**
     * Validation of the identifier via regular expression.
     */
    VALIDATE_IDENTIFIER_REGEX(new Parameter<>("validateIdentifierRegex", "[\\w|-]")),

    /**
     * Colours used to represent the issues in the calendar editor.
     */
    ISSUE_COLOURS(new Parameter<>("issue.colours",
            "#f94a15;#0071bc;#42ba37;#ee7e5b;#1e3946;#ca2f00;#AAAAFF;#000055;#0000FF;#FFFF00;#000000")),

    /**
     * Number of pages per process below which the features in the granularity
     * dialog shall be locked. Long.
     */
    MINIMAL_NUMBER_OF_PAGES(new Parameter<UndefinedParameter>("numberOfPages.minimum")),

    /*
     * Batch processing
     */

    /**
     * Limits the number of batches showing on the page “Batches”. Defaults to -1
     * which disables this functionality. If set, only the limited number of batches
     * will be shown, the other batches will be present but hidden and thus cannot
     * be modified and not even be deleted. Integer.
     */
    BATCH_DISPLAY_LIMIT(new Parameter<>("batchMaxSize", -1)),

    /**
     * Turn on or off whether each assignment of processes to or removal from
     * batches shall result in rewriting each processes' wiki field in order to
     * leave a note there. Enabling this function may slow down operations in the
     * batches dialogue. Boolean, defaults to {@code false}.
     */
    BATCHES_LOG_CHANGES(new Parameter<>("batches.logChangesToWikiField", false)),

    /*
     * Task manager
     */

    /**
     * Overrides the limit of tasks run in parallel. Integer, defaults to the number
     * of available cores.
     */
    TASK_MANAGER_AUTORUN_LIMIT(new Parameter<UndefinedParameter>("taskManager.autoRunLimit")),

    /**
     * Sets the time interval between two inspections of the task list. Long,
     * defaults to 2000 ms.
     */
    TASK_MANAGER_INSPECTION_INTERVAL_MILLIS(new Parameter<>("taskManager.inspectionIntervalMillis",
            TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS))),

    /**
     * Sets the maximum number of failed threads to keep around in RAM. Keep in mind
     * that zombie processes still occupy all their resources and aren't available
     * for garbage collection, so choose these values as restrictive as possible.
     * Integer, defaults to 10.
     */
    TASK_MANAGER_KEEP_FAILED(new Parameter<>("taskManager.keepThreads.failed.count", 10)),

    /**
     * Sets the maximum time to keep failed threads around in RAM. Keep in mind that
     * zombie processes still occupy all their resources and aren't available for
     * garbage collection, so choose these values as restrictive as possible.
     * Integer, defaults to 240 minutes.
     */
    TASK_MANAGER_KEEP_FAILED_MINS(
            new Parameter<>("taskManager.keepThreads.failed.minutes", TimeUnit.MINUTES.convert(4, TimeUnit.HOURS))),

    /**
     * Sets the maximum number of successfully finished threads to keep around in
     * RAM. Defaults to 3. Keep in mind that zombie processes still occupy all their
     * resources and aren't available for garbage collection, so choose these values
     * as restrictive as possible.
     */
    TASK_MANAGER_KEEP_SUCCESSFUL(new Parameter<>("taskManager.keepThreads.successful.count", 3)),

    /**
     * Sets the maximum time to keep successfully finished threads around in RAM.
     * Defaults to 20 minutes. Keep in mind that zombie processes still occupy all
     * their resources and aren't available for garbage collection, so choose these
     * values as restrictive as possible.
     */
    TASK_MANAGER_KEEP_SUCCESSFUL_MINS(new Parameter<>("taskManager.keepThreads.successful.minutes",
            TimeUnit.MINUTES.convert(20, TimeUnit.MINUTES))),

    /**
     * Sets whether or not to show an option to "add a sample task" in the task
     * manager. This is if for anything at all—useful for debugging or demonstration
     * purposes only. Boolean, defaults to {@code false}.
     */
    TASK_MANAGER_SHOW_SAMPLE_TASK(new Parameter<>("taskManager.showSampleTask", false)),

    /*
     * Export to presentation module
     */

    /**
     * If you set this to true the exports will be done asynchronously (in the
     * background). This requires that the automatic export was set up in the
     * project settings. Boolean, defaults to {@code false}.
     */
    ASYNCHRONOUS_AUTOMATIC_EXPORT(new Parameter<>("asynchronousAutomaticExport", false)),

    /**
     * Whether during an export to the DMS the images will be copied. Boolean,
     * defaults to {@code true}.
     */
    EXPORT_WITH_IMAGES(new Parameter<>("automaticExportWithImages", true)),

    /**
     * Boolean, defaults to {@code true}.
     */
    AUTOMATIC_EXPORT_WITH_OCR(new Parameter<>("automaticExportWithOcr", true)),

    /**
     * Boolean, defaults to {@code true}.
     */
    EXPORT_VALIDATE_IMAGES(new Parameter<>("ExportValidateImages", true)),

    /**
     * Boolean, defaults to {@code false}.
     */
    EXPORT_WITHOUT_TIME_LIMIT(new Parameter<>("exportWithoutTimeLimit", true)),

    /*
     * REMOTE SERVICES
     *
     * LDAP Configuration
     */

    /**
     * Boolean, defaults to {@code true}.
     */
    LDAP_USE(new Parameter<>("ldap_use", true)),

    LDAP_ATTRIBUTE_TO_TEST(new Parameter<UndefinedParameter>("ldap_AttributeToTest")),

    LDAP_VALUE_OF_ATTRIBUTE(new Parameter<UndefinedParameter>("ldap_ValueOfAttribute")),

    /**
     * Boolean, defaults to {@code true}.
     */
    LDAP_USE_LOCAL_DIRECTORY(new Parameter<>("useLocalDirectory", true)),

    /**
     * Boolean, defaults to {@code false}.
     */
    LDAP_USE_TLS(new Parameter<>("ldap_useTLS", false)),

    LDAP_USE_SIMPLE_AUTH(new Parameter<UndefinedParameter>("useSimpleAuthentification")),

    /*
     * Authority control configuration
     */

    /**
     * Which authority identifier to use for a given URI prefix.
     *
     * <p>
     * Example: authority.http\://d-nb.info/gnd/.id=gnd
     */
    AUTHORITY_ID_FROM_URI(new Parameter<UndefinedParameter>("authority.{0}.id")),

    /**
     * Content to put in the URI field when adding a new metadata element of type
     * person. This should usually be your preferred norm data file’s URI prefix as
     * to the user doesn’t have to enter it over and over again.
     *
     * <p>
     * Example: authority.default=http\://d-nb.info/gnd/
     */
    AUTHORITY_DEFAULT(new Parameter<UndefinedParameter>("authority.default")),

    /*
     * FUNCTIONAL EXTENSIONS
     *
     * OCR service access
     */

    /**
     * Boolean, defaults to {@code false}.
     */
    SHOW_OCR_BUTTON(new Parameter<>("showOcrButton", false)),

    /**
     * Base path to OCR, without parameters.
     */
    OCR_URL(new Parameter<UndefinedParameter>("ocrUrl")),

    /**
     * Process properties to display in process list.
     */
    PROCESS_PROPERTIES(new Parameter<>("processPropertyColumns")),

    /**
     * Default client parameter to be returned if no session client could be determined by user service.
     */
    DEFAULT_CLIENT_ID(new Parameter<>("defaultClientId", 0)),

    /**
     * Parameter controlling whether each process in the system needs to have a unique name or not.
     */
    UNIQUE_PROCESS_TITLES(new Parameter<>("uniqueProcessTitles", true)),

    /**
     * Task custom columns to display in current tasks list.
     */
    TASK_CUSTOM_COLUMNS(new Parameter<>("taskProcessPropertyColumns")),

    /*
     * ActiveMQ web services
     */

    ACTIVE_MQ_HOST_URL(new Parameter<UndefinedParameter>("activeMQ.hostURL")),

    ACTIVE_MQ_CREATE_NEW_PROCESSES_QUEUE(new Parameter<UndefinedParameter>("activeMQ.createNewProcess.queue")),

    ACTIVE_MQ_FINALIZE_STEP_QUEUE(new Parameter<UndefinedParameter>("activeMQ.finaliseStep.queue")),

    ACTIVE_MQ_RESULTS_TOPIC(new Parameter<UndefinedParameter>("activeMQ.results.topic")),

    /**
     * Long, value in milliseconds.
     */
    ACTIVE_MQ_RESULTS_TTL(new Parameter<>("activeMQ.results.timeToLive",
            TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS))),

    /*
     * Elasticsearch properties
     */

    ELASTICSEARCH_BATCH(new Parameter<>("elasticsearch.batch", 500)),
    ELASTICSEARCH_INDEXLIMIT(new Parameter<>("elasticsearch.indexLimit", 5000));

    private Parameter parameter;

    /**
     * Private constructor to hide the implicit public one.
     *
     * @param parameter
     *            for config
     */
    ParameterCore(Parameter parameter) {
        this.parameter = parameter;
    }

    /**
     * Get parameter.
     *
     * @return value of parameter
     */
    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public String toString() {
        return this.parameter.getKey();
    }
}
