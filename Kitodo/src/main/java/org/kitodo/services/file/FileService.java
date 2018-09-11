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

package org.kitodo.services.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.io.BackupFileRotation;
import org.kitodo.api.command.CommandResult;
import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.MetadataFormat;
import org.kitodo.helper.Helper;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.command.CommandService;
import org.kitodo.services.data.RulesetService;

public class FileService {

    private static final Logger logger = LogManager.getLogger(FileService.class);
    private static final String TEMPORARY_FILENAME_PREFIX = "temporary_";
    private static final ServiceManager serviceManager = new ServiceManager();

    /**
     * Creates a MetaDirectory.
     *
     * @param parentFolderUri
     *            The URI, where the
     * @param directoryName
     *            the name of the directory
     * @return true or false
     * @throws IOException
     *             an IOException
     */
    public boolean createMetaDirectory(URI parentFolderUri, String directoryName) throws IOException {
        if (!fileExist(parentFolderUri.resolve(directoryName))) {
            CommandService commandService = serviceManager.getCommandService();
            String path = FileSystems.getDefault()
                    .getPath(ConfigCore.getKitodoDataDirectory(), parentFolderUri.getRawPath(), directoryName)
                    .normalize().toAbsolutePath().toString();
            List<String> commandParameter = Collections.singletonList(path);
            File script = new File(ConfigCore.getParameter(Parameters.SCRIPT_CREATE_DIR_META));
            CommandResult commandResult = commandService.runCommand(script, commandParameter);
            return commandResult.isSuccessful();
        } else {
            logger.info("Metadata directory: " + directoryName + " already existed! No new directory was created");
            return true;
        }
    }

    /**
     * Creates a directory at a given URI with a given name.
     *
     * @param parentFolderUri
     *            the uri, where the directory should be created
     * @param directoryName
     *            the name of the directory.
     * @return the URI of the new directory or URI of parent directory if
     *         directoryName is null or empty
     */
    public URI createDirectory(URI parentFolderUri, String directoryName) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        if (directoryName != null) {
            return fileManagementModule.create(parentFolderUri, directoryName, false);
        }
        return URI.create("");
    }

    /**
     * Creates a directory with a name given and assigns permissions to the
     * given user. Under Linux a script is used to set the file system
     * permissions accordingly. This cannot be done from within java code before
     * version 1.7.
     *
     * @param dirName
     *            Name of directory to create
     * @throws IOException
     *             If an I/O error occurs.
     */
    public void createDirectoryForUser(URI dirName, String userName) throws IOException {
        if (!serviceManager.getFileService().fileExist(dirName)) {

            CommandService commandService = serviceManager.getCommandService();
            List<String> commandParameter = Arrays.asList(userName, new File(dirName).getPath());
            commandService.runCommand(new File(ConfigCore.getParameter(Parameters.SCRIPT_CREATE_DIR_USER_HOME)),
                commandParameter);
        }
    }

    /**
     * Creates a new File.
     *
     * @param fileName
     *            the name of the new file
     * @return the uri of the new file
     */
    public URI createResource(String fileName) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.create(null, fileName, true);
    }

    /**
     * Creates a resource at a given URI with a given name.
     *
     * @param targetFolder
     *            the URI of the target folder
     * @param name
     *            the name of the new resource
     * @return the URI of the created resource
     */
    public URI createResource(URI targetFolder, String name) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.create(targetFolder, name, true);
    }

    /**
     * Writes to a file at a given URI.
     *
     * @param uri
     *            the URI, to write to.
     * @return an output stream to the file at the given URI or null
     * @throws IOException
     *             if write fails
     */
    public OutputStream write(URI uri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.write(uri);
    }

    /**
     * Reads a file at a given URI.
     *
     * @param uri
     *            the uri to read
     * @return an InputStream to read from or null
     * @throws IOException
     *             if read fails
     */
    public InputStream read(URI uri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.read(uri);
    }

    /**
     * This function implements file renaming. Renaming of files is full of
     * mischief under Windows which unaccountably holds locks on files.
     * Sometimes running the JVM’s garbage collector puts things right.
     *
     * @param fileUri
     *            File to rename
     * @param newFileName
     *            New file name / destination
     * @throws IOException
     *             is thrown if the rename fails permanently
     */
    public URI renameFile(URI fileUri, String newFileName) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.rename(fileUri, newFileName);
    }

    /**
     * Calculate all files with given file extension at specified directory
     * recursively.
     *
     * @param directory
     *            the directory to run through
     * @return number of files as Integer
     */
    public Integer getNumberOfFiles(URI directory) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getNumberOfFiles(null, directory);
    }

    /**
     * Calculate all files with given file extension at specified directory
     * recursively.
     *
     * @param directory
     *            the directory to run through
     * @return number of files as Integer
     */
    public Integer getNumberOfImageFiles(URI directory) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getNumberOfFiles(Helper.imageNameFilter, directory);
    }

    /**
     * Get size of directory.
     *
     * @param directory
     *            URI to get size
     * @return size of directory as Long
     */
    public Long getSizeOfDirectory(URI directory) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getSizeOfDirectory(directory);
    }

    /**
     * Copy directory.
     *
     * @param sourceDirectory
     *            source file as uri
     * @param targetDirectory
     *            destination file as uri
     */
    public void copyDirectory(URI sourceDirectory, URI targetDirectory) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        fileManagementModule.copy(sourceDirectory, targetDirectory);
    }

    /**
     * Copies a file from a given URI to a given URI.
     *
     * @param sourceUri
     *            the uri to copy from
     * @param destinationUri
     *            the uri to copy to
     * @throws IOException
     *             if copying fails
     */
    public void copyFile(URI sourceUri, URI destinationUri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        fileManagementModule.copy(sourceUri, destinationUri);
    }

    /**
     * Copies a file to a directory.
     *
     * @param sourceDirectory
     *            The source directory
     * @param targetDirectory
     *            the target directory
     * @throws IOException
     *             if copying fails.
     */
    public void copyFileToDirectory(URI sourceDirectory, URI targetDirectory) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        fileManagementModule.copy(sourceDirectory, targetDirectory);
    }

    /**
     * Deletes a resource at a given URI.
     *
     * @param uri
     *            the uri to delete
     * @return true, if successful, false otherwise
     * @throws IOException
     *             if get of module fails
     */
    public boolean delete(URI uri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.delete(uri);
    }

    /**
     * Checks, if a file exists.
     *
     * @param uri
     *            the URI, to check, if there is a file
     * @return true, if the file exists
     */
    public boolean fileExist(URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.fileExist(uri);
    }

    /**
     * Checks if a resource at a given URI is a file.
     *
     * @param uri
     *            the URI to check, if there is a file
     * @return true, if it is a file, false otherwise
     */
    public boolean isFile(URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.isFile(uri);
    }

    /**
     * checks, if a URI leads to a directory.
     *
     * @param dir
     *            the uri to check.
     * @return true, if it is a directory.
     */
    public boolean isDirectory(URI dir) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.isDirectory(dir);
    }

    /**
     * Checks if an uri is readable.
     *
     * @param uri
     *            the uri to check.
     * @return true, if it's readable, false otherwise.
     */
    public boolean canRead(URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.canRead(uri);
    }

    /**
     * Returns the name of a file at a given URI.
     *
     * @param uri
     *            the URI, to get the filename from.
     * @return the name of the file
     */
    public String getFileName(URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        String fileNameWithExtension = fileManagementModule.getFileNameWithExtension(uri);
        if (fileNameWithExtension.contains(".")) {
            return fileNameWithExtension.substring(0, fileNameWithExtension.indexOf('.'));
        }
        return fileNameWithExtension;
    }

    /**
     * Returns the name of a file at a given uri.
     *
     * @param uri
     *            the URI, to get the filename from
     * @return the name of the file
     */
    public String getFileNameWithExtension(URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getFileNameWithExtension(uri);
    }

    /**
     * Moves a directory from a given URI to a given URI.
     *
     * @param sourceUri
     *            the source URI
     * @param targetUri
     *            the target URI
     * @throws IOException
     *             if get of module fails
     */
    public void moveDirectory(URI sourceUri, URI targetUri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        fileManagementModule.move(sourceUri, targetUri);
    }

    /**
     * Moves a file from a given URI to a given URI.
     *
     * @param sourceUri
     *            the source URI
     * @param targetUri
     *            the target URI
     * @throws IOException
     *             if get of module fails
     */
    public void moveFile(URI sourceUri, URI targetUri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        fileManagementModule.move(sourceUri, targetUri);
    }

    /**
     * Get all sub URIs of an URI.
     *
     * @param uri
     *            the URI, to get the sub URIs from
     * @return a List of sub URIs
     */
    public List<URI> getSubUris(URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getSubUris(null, uri);
    }

    /**
     * Get all sub URIs of an URI with a given filter.
     *
     * @param filter
     *            the filter to filter the sub URIs
     * @param uri
     *            the URI, to get the sub URIs from
     * @return a List of sub URIs
     */
    public List<URI> getSubUris(FilenameFilter filter, URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getSubUris(filter, uri);
    }

    /**
     * Lists all Files at the given Path.
     *
     * @param file
     *            the directory to get the Files from
     * @return an Array of Files.
     */
    private File[] listFiles(File file) {
        File[] unchecked = file.listFiles();
        return unchecked != null ? unchecked : new File[0];
    }

    /**
     * Writes a metadata file.
     *
     * @param gdzfile
     *            the file format
     * @param process
     *            the process
     * @throws IOException
     *             if error occurs
     * @throws PreferencesException
     *             if error occurs
     * @throws WriteException
     *             if error occurs
     */
    public void writeMetadataFile(FileformatInterface gdzfile, Process process)
            throws IOException, PreferencesException, WriteException {
        RulesetService rulesetService = serviceManager.getRulesetService();
        FileformatInterface ff;

        Ruleset ruleset = process.getRuleset();
        switch (MetadataFormat.findFileFormatsHelperByName(process.getProject().getFileFormatInternal())) {
            case METS:
                ff = UghImplementation.INSTANCE.createMetsMods(rulesetService.getPreferences(ruleset));
                break;
            case RDF:
                ff = UghImplementation.INSTANCE.createRDFFile(rulesetService.getPreferences(ruleset));
                break;
            default:
                ff = UghImplementation.INSTANCE.createXStream(rulesetService.getPreferences(ruleset));
                break;
        }
        // createBackupFile();
        URI metadataFileUri = getMetadataFilePath(process);
        String temporaryMetadataFileName = getTemporaryMetadataFileName(metadataFileUri);

        ff.setDigitalDocument(gdzfile.getDigitalDocument());
        // ff.write(getMetadataFilePath());
        ff.write(temporaryMetadataFileName);
        File temporaryMetadataFile = new File(temporaryMetadataFileName);
        boolean backupCondition = temporaryMetadataFile.exists() && (temporaryMetadataFile.length() > 0);
        if (backupCondition) {
            createBackupFile(process);
            renameFile(Paths.get(temporaryMetadataFileName).toUri(), metadataFileUri.getRawPath());
            removePrefixFromRelatedMetsAnchorFilesFor(Paths.get(temporaryMetadataFileName).toUri());
        }
    }

    private void removePrefixFromRelatedMetsAnchorFilesFor(URI temporaryMetadataFilename) throws IOException {
        File temporaryFile = new File(temporaryMetadataFilename);
        File directoryPath = new File(temporaryFile.getParentFile().getPath());
        for (File temporaryAnchorFile : listFiles(directoryPath)) {
            String temporaryAnchorFileName = temporaryAnchorFile.toString();
            if (temporaryAnchorFile.isFile()
                    && FilenameUtils.getBaseName(temporaryAnchorFileName).startsWith(TEMPORARY_FILENAME_PREFIX)) {
                String anchorFileName = FilenameUtils.concat(FilenameUtils.getFullPath(temporaryAnchorFileName),
                    temporaryAnchorFileName.replace(TEMPORARY_FILENAME_PREFIX, ""));
                temporaryAnchorFileName = FilenameUtils.concat(FilenameUtils.getFullPath(temporaryAnchorFileName),
                    temporaryAnchorFileName);
                renameFile(Paths.get(temporaryAnchorFileName).toUri(), new File(anchorFileName).toURI().getRawPath());
            }
        }
    }

    // backup of meta.xml
    void createBackupFile(Process process) throws IOException {
        int numberOfBackups;

        numberOfBackups = ConfigCore.getIntParameter(Parameters.NUMBER_OF_META_BACKUPS);

        if (numberOfBackups != ConfigCore.INT_PARAMETER_NOT_DEFINED_OR_ERRONEOUS) {
            BackupFileRotation bfr = new BackupFileRotation();
            bfr.setNumberOfBackups(numberOfBackups);
            bfr.setFormat("meta.*\\.xml");
            bfr.setProcess(process);
            bfr.performBackup();
        } else {
            logger.warn("No backup configured for meta data files.");
        }
    }

    /**
     * Gets the URI of the metadata.xml of a given process.
     *
     * @param process
     *            the process to get the metadata.xml for.
     * @return The URI to the metadata.xml
     */
    public URI getMetadataFilePath(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.META_XML, null);
    }

    private String getTemporaryMetadataFileName(URI fileName) {
        File temporaryFile = getFile(fileName);
        String directoryPath = temporaryFile.getParentFile().getPath();
        String temporaryFileName = TEMPORARY_FILENAME_PREFIX + temporaryFile.getName();

        return directoryPath + File.separator + temporaryFileName;
    }

    /**
     * Gets the specific IMAGE sub type.
     *
     * @param process
     *            the process to get the imageDirectory for.
     * @return The uri of the Image Directory.
     */
    public URI getImagesDirectory(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.IMAGE, null);
    }

    /**
     * Gets the URI to the ocr directory.
     *
     * @param process
     *            the process tog et the ocr directory for.
     * @return the uri to the ocr directory.
     */
    public URI getOcrDirectory(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.OCR, null);
    }

    /**
     * Gets the URI to the import directory.
     *
     * @param process
     *            the process to get the import directory for.
     * @return the uri of the import directory
     */
    public URI getImportDirectory(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.IMPORT, null);
    }

    /**
     * Gets the URI to the text directory.
     *
     * @param process
     *            the process to get the text directory for.
     * @return the uri of the text directory
     */
    public URI getTxtDirectory(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.OCR_TXT, null);
    }

    /**
     * Gets the URI to the pdf directory.
     *
     * @param process
     *            the process to get the pdf directory for.
     * @return the uri of the pdf directory
     */
    public URI getPdfDirectory(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.OCR_PDF, null);
    }

    /**
     * Gets the URI to the alto directory.
     *
     * @param process
     *            the process to get the alto directory for.
     * @return the uri of the alto directory
     */
    public URI getAltoDirectory(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.OCR_ALTO, null);
    }

    /**
     * Gets the URI to the word directory.
     *
     * @param process
     *            the process to get the word directory for.
     * @return the uri of the word directory
     */
    public URI getWordDirectory(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.OCR_WORD, null);
    }

    /**
     * Gets the URI to the template file.
     *
     * @param process
     *            the process to get the template file for.
     * @return the uri of the template file
     */
    public URI getTemplateFile(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.TEMPLATE, null);
    }

    /**
     * This method is needed for migration purposes. It maps existing filePaths
     * to the correct URI. File.separator doesn't work because on Windows it
     * appends backslash to URI.
     *
     * @param process
     *            the process, the uri is needed for.
     * @return the URI.
     */
    public URI getProcessBaseUriForExistingProcess(Process process) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        URI processBaseUri = process.getProcessBaseUri();
        if (processBaseUri == null && process.getId() != null) {
            process.setProcessBaseUri(fileManagementModule.createUriForExistingProcess(process.getId().toString()));
        }
        return process.getProcessBaseUri();
    }

    /**
     * Get the URI for a process sub-location. Possible locations are listed in
     * ProcessSubType.
     *
     * @param processId
     *            the id of process to get the sublocation for
     * @param processTitle
     *            the title of process to get the sublocation for
     * @param processDataDirectory
     *            the base URI of process to get the sublocation for
     * @param processSubType
     *            The subType.
     * @param resourceName
     *            the name of the single object (e.g. image) if null, the root
     *            folder of the sublocation is returned
     * @return The URI of the requested location
     */
    public URI getProcessSubTypeURI(Integer processId, String processTitle, URI processDataDirectory,
            ProcessSubType processSubType, String resourceName) throws DAOException {

        if (processDataDirectory == null) {
            Process process = serviceManager.getProcessService().getById(processId);
            processDataDirectory = serviceManager.getProcessService().getProcessDataDirectory(process);
        }

        if (resourceName == null) {
            resourceName = "";
        }
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getProcessSubTypeUri(processDataDirectory, processTitle, processSubType,
            resourceName);
    }

    /**
     * Get's the URI for a Process Sub-location. Possible Locations are listed
     * in ProcessSubType
     *
     * @param process
     *            the process to get the sublocation for.
     * @param processSubType
     *            The subType.
     * @param resourceName
     *            the name of the single object (e.g. image) if null, the root
     *            folder of the sublocation is returned
     * @return The URI of the requested location
     */
    public URI getProcessSubTypeURI(Process process, ProcessSubType processSubType, String resourceName) {

        URI processDataDirectory = serviceManager.getProcessService().getProcessDataDirectory(process);

        if (resourceName == null) {
            resourceName = "";
        }
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getProcessSubTypeUri(processDataDirectory, process.getTitle(), processSubType,
            resourceName);
    }

    /**
     * Get part of the URI for specific process.
     *
     * @param filter
     *            FilenameFilter object
     * @param processId
     *            the id of process
     * @param processTitle
     *            the title of process
     * @param processDataDirectory
     *            the base URI of process
     * @param processSubType
     *            object
     * @param resourceName
     *            as String
     * @return unmapped URI
     */
    public List<URI> getSubUrisForProcess(FilenameFilter filter, Integer processId, String processTitle,
            URI processDataDirectory, ProcessSubType processSubType, String resourceName) throws DAOException {
        URI processSubTypeURI = getProcessSubTypeURI(processId, processTitle, processDataDirectory, processSubType,
            resourceName);
        return getSubUris(filter, processSubTypeURI);
    }

    /**
     * Get part of the URI for specific process.
     *
     * @param filter
     *            FilenameFilter object
     * @param process
     *            object
     * @param processSubType
     *            object
     * @param resourceName
     *            as String
     * @return unmapped URI
     */
    public List<URI> getSubUrisForProcess(FilenameFilter filter, Process process, ProcessSubType processSubType,
            String resourceName) {
        URI processSubTypeURI = getProcessSubTypeURI(process, processSubType, resourceName);
        return getSubUris(filter, processSubTypeURI);
    }

    /**
     * Deletes all process directories and their content.
     *
     * @param process
     *            the process to delete the directories for.
     * @return true, if deletion was successful.
     */
    public boolean deleteProcessContent(Process process) throws IOException {
        for (ProcessSubType processSubType : ProcessSubType.values()) {
            URI processSubTypeURI = getProcessSubTypeURI(process, processSubType, null);
            FileManagementInterface fileManagementModule = getFileManagementModule();
            if (!fileManagementModule.delete(processSubTypeURI)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the image source directory.
     *
     * @param process
     *            the process, to get the source directory for
     * @return the source directory as an URI
     */
    public URI getSourceDirectory(Process process) {
        return getProcessSubTypeURI(process, ProcessSubType.IMAGE_SOURCE, null);
    }

    /**
     * Gets the URI to the temporal directory.
     *
     * @return the URI to the temporal directory.
     */
    public URI getTemporaryDirectory() {
        return ConfigCore.getUri(Parameters.DIR_TEMP);
    }

    /**
     * Gets the URI to the users directory.
     *
     * @return the URI to the users directory.
     */
    public URI getUsersDirectory() {
        return ConfigCore.getUri(Parameters.DIR_USERS);
    }

    public void writeMetadataAsTemplateFile(FileformatInterface inFile, Process process)
            throws WriteException, PreferencesException {
        inFile.write(getTemplateFile(process).toString());
    }

    /**
     * Creates a symbolic link.
     *
     * @param targetUri
     *            the target URI for the link
     * @param homeUri
     *            the home URI
     * @return true, if link creation was successful
     */
    public boolean createSymLink(URI homeUri, URI targetUri, boolean onlyRead, User user) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.createSymLink(homeUri, targetUri, onlyRead, user.getLogin());
    }

    /**
     * Delete a symbolic link.
     *
     * @param homeUri
     *            the URI of the home folder, where the link should be deleted
     * @return true, if deletion was successful
     */
    public boolean deleteSymLink(URI homeUri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.deleteSymLink(homeUri);
    }

    public File getFile(URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getFile(uri);
    }

    private FileManagementInterface getFileManagementModule() {
        KitodoServiceLoader<FileManagementInterface> loader = new KitodoServiceLoader<>(FileManagementInterface.class);
        return loader.loadModule();
    }

    /**
     * Deletes the slash as first character from an uri object.
     *
     * @param uri
     *            The uri object.
     * @return The new uri object without first slash.
     */
    public URI deleteFirstSlashFromPath(URI uri) {
        String uriString = uri.getPath();
        if (uriString.startsWith("/")) {
            uriString = uriString.replaceFirst("/", "");
        }
        return URI.create(uriString);
    }

    /**
     * Creates images files by copy of a configured source dummy image at images source folder of given process.
     * 
     * @param process
     *            The process object.
     * @param numberOfNewImages
     *            The number of images to be created.
     */
    public void createDummyImagesForProcess(Process process, int numberOfNewImages) throws IOException, URISyntaxException {
        URI imagesDirectory = getSourceDirectory(process);
        int startValue = serviceManager.getFileService().getNumberOfFiles(imagesDirectory) + 1;
        URI dummyImage = getDummyImagePath();

        // Load number of digits to create valid filenames
        String numberOfDigits = extractNumber(ConfigCore.getParameter("ImagePrefix"));

        for (int i = startValue; i < startValue + numberOfNewImages; i++) {
            copyFile(dummyImage, imagesDirectory.resolve(String.format("%0" + numberOfDigits + "d", i) + ".tif"));
        }
    }

    private String extractNumber(String string) {
        return string.replaceAll("\\D+","");
    }

    private URI getDummyImagePath() throws URISyntaxException, IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL dummyImage = classloader.getResource("images/dummyImage.tif");
        if (Objects.nonNull(dummyImage)) {
            return dummyImage.toURI();
        } else {
            throw new IOException("No dummy image found in resources!");
        }
    }
}
