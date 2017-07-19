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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ShellScript;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.io.BackupFileRotation;
import org.hibernate.Hibernate;
import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.filemanagement.filters.FileNameEndsWithFilter;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.helper.enums.MetadataFormat;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.RulesetService;

import ugh.dl.Fileformat;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.WriteException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.XStream;

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
     * @throws IOException
     *             an IOException
     */
    public void createMetaDirectory(URI parentFolderUri, String directoryName) throws IOException {
        if (!fileExist(parentFolderUri.resolve(directoryName))) {
            ShellScript createDirScript = new ShellScript(new File(ConfigCore.getParameter("script_createDirMeta")));
            createDirScript.run(Collections.singletonList(parentFolderUri + directoryName));
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
            ShellScript createDirScript = new ShellScript(
                    new File(ConfigCore.getParameter("script_createDirUserHome")));
            createDirScript.run(Arrays.asList(userName, new File(dirName).getPath()));
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
     * This function implements file renaming. Renaming of files is full of mischief
     * under Windows which unaccountably holds locks on files. Sometimes running the
     * JVM’s garbage collector puts things right.
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
    public Integer getNumberOfFiles(URI directory) throws IOException {
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
    public Integer getNumberOfImageFiles(URI directory) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getNumberOfFiles(Helper.imageNameFilter, directory);
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
     * @throws IOException
     *             if get of module fails
     */
    public boolean fileExist(URI uri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.fileExist(uri);
    }

    /**
     * Checks if a resource at a given URI is a file.
     *
     * @param uri
     *            the URI to check, if there is a file
     * @return true, if it is a file, false otherwise
     * @throws IOException
     *             if get of module fails
     */
    public boolean isFile(URI uri) throws IOException {
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
    public boolean isDirectory(URI dir) throws IOException {
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
    public boolean canRead(URI uri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.canRead(uri);
    }

    /**
     * Returns the name of a file at a given URI.
     *
     * @param uri
     *            the URI, to get the filename from.
     * @return the name of the file
     * @throws IOException
     *             if get of module fails
     */
    public String getFileName(URI uri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        String fileNameWithExtension = fileManagementModule.getFileNameWithExtension(uri);
        if (fileNameWithExtension.contains(".")) {
            return fileNameWithExtension.substring(0, fileNameWithExtension.indexOf("."));
        }
        return fileNameWithExtension;
    }

    /**
     * Returns the name of a file at a given uri.
     *
     * @param uri
     *            the URI, to get the filename from
     * @return the name of the file
     * @throws IOException
     *             if get of module fails
     */
    public String getFileNameWithExtension(URI uri) throws IOException {
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
    public ArrayList<URI> getSubUris(URI uri) throws IOException {
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
    public ArrayList<URI> getSubUris(FilenameFilter filter, URI uri) throws IOException {
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
    public void writeMetadataFile(Fileformat gdzfile, Process process)
            throws IOException, PreferencesException, WriteException {
        serviceManager.getFileService().write(serviceManager.getProcessService().getProcessDataDirectory(process))
                .close();

        RulesetService rulesetService = new RulesetService();
        Fileformat ff;
        URI metadataFileUri;

        Hibernate.initialize(process.getRuleset());
        switch (MetadataFormat.findFileFormatsHelperByName(process.getProject().getFileFormatInternal())) {
            case METS:
                ff = new MetsMods(rulesetService.getPreferences(process.getRuleset()));
                break;
            case RDF:
                ff = new RDFFile(rulesetService.getPreferences(process.getRuleset()));
                break;
            default:
                ff = new XStream(rulesetService.getPreferences(process.getRuleset()));
                break;
        }
        // createBackupFile();
        metadataFileUri = getMetadataFilePath(process);
        String temporaryMetadataFileName = getTemporaryMetadataFileName(metadataFileUri);

        ff.setDigitalDocument(gdzfile.getDigitalDocument());
        // ff.write(getMetadataFilePath());
        boolean writeResult = ff.write(temporaryMetadataFileName);
        File temporaryMetadataFile = new File(temporaryMetadataFileName);
        boolean backupCondition = writeResult && temporaryMetadataFile.exists() && (temporaryMetadataFile.length() > 0);
        if (backupCondition) {
            createBackupFile(process);
            renameFile(metadataFileUri, temporaryMetadataFileName);
            removePrefixFromRelatedMetsAnchorFilesFor(URI.create(temporaryMetadataFileName));
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
                renameFile(URI.create(anchorFileName), temporaryAnchorFileName);
            }
        }
    }

    // backup of meta.xml
    void createBackupFile(Process process) throws IOException {
        int numberOfBackups;

        numberOfBackups = ConfigCore.getIntParameter("numberOfMetaBackups");

        if (numberOfBackups != 0) {
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
        return mapUriToKitodoDataDirectoryUri(getProcessSubTypeURI(process, ProcessSubType.META_XML, null));
    }

    private String getTemporaryMetadataFileName(URI fileName) {

        File temporaryFile = new File(fileName);
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
        String path = process.getId().toString();
        return URI.create(path);
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
        try {
            FileManagementInterface fileManagementModule = getFileManagementModule();
            return fileManagementModule.getProcessSubTypeUri(processDataDirectory, process.getTitle(), processSubType,
                    resourceName);
        } catch (IOException e) {
            logger.error(e);
            return URI.create("");
        }
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
    public ArrayList<URI> getSubUrisForProcess(FilenameFilter filter, Process process, ProcessSubType processSubType,
            String resourceName) {
        ArrayList<URI> subURIs = new ArrayList<>();
        try {
            URI processSubTypeURI = getProcessSubTypeURI(process, processSubType, resourceName);
            subURIs = getSubUris(filter, processSubTypeURI);
        } catch (IOException e) {
            logger.error(e);
        }
        return subURIs;
    }

    /**
     * Deletes all process directories and their content.
     *
     * @param process
     *            the process to delete the directories for.
     * @return true, if deletion was successful.
     */
    public boolean deleteProcessContent(Process process) {
        for (ProcessSubType processSubType : ProcessSubType.values()) {
            URI processSubTypeURI = getProcessSubTypeURI(process, processSubType, null);
            try {
                FileManagementInterface fileManagementModule = getFileManagementModule();
                fileManagementModule.delete(processSubTypeURI);
            } catch (IOException e) {
                logger.warn("uri " + processSubTypeURI + " could not be deleted");
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
     * @return the source directory as a string
     */
    public URI getSourceDirectory(Process process) {
        URI dir = getProcessSubTypeURI(process, ProcessSubType.IMAGE, null);
        FilenameFilter filterDirectory = new FileNameEndsWithFilter("_source");
        URI sourceFolder = URI.create("");
        try {
            ArrayList<URI> directories = getSubUris(filterDirectory, dir);
            if (directories == null || directories.size() == 0) {
                sourceFolder = dir.resolve(process.getTitle() + "_source");
                if (ConfigCore.getBooleanParameter("createSourceFolder", false)) {
                    createDirectory(dir, process.getTitle() + "_source");
                }
            } else {
                sourceFolder = dir.resolve(directories.get(0));
            }
        } catch (IOException e) {
            logger.error(e);
        }

        return sourceFolder;
    }

    /**
     * Map relative URI to absolute kitodo data directory URI.
     * 
     * @param uri
     *            relative path
     * @return absolute URI path
     */
    public URI mapUriToKitodoDataDirectoryUri(URI uri) {
        String kitodoDataDirectory = ConfigCore.getKitodoDataDirectory();
        if (!uri.isAbsolute() && !uri.toString().contains(kitodoDataDirectory)) {
            return Paths.get(ConfigCore.getKitodoDataDirectory(), uri.toString()).toUri();
        }
        return uri;
    }

    URI unmapUriFromKitodoDataDirectoryUri(URI uri) {
        return unmapDirectory(uri, ConfigCore.getKitodoDataDirectory());
    }

    private URI unmapDirectory(URI uri, String directory) {
        String path = uri.toString();
        directory = encodeDirectory(directory);
        if (path.contains(directory)) {
            String[] split = path.split(directory);
            String shortUri = split[1];
            return URI.create(shortUri);
        }
        return uri;
    }

    private String encodeDirectory(String directory) {
        if (directory.contains("\\")) {
            directory = directory.replace("\\", "/");
        }
        return directory;
    }

    /**
     * Gets the URI to the temporal directory.
     *
     * @return the URI to the temporal directory.
     */
    public URI getTemporalDirectory() {
        return Paths.get(ConfigCore.getParameter("tempfolder", "/usr/local/kitodo/tmp/")).toUri();
    }

    public void writeMetadataAsTemplateFile(Fileformat inFile, Process process)
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
     * @throws IOException
     *             if get of module fails
     */
    public boolean createSymLink(URI homeUri, URI targetUri, boolean onlyRead, User user) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.createSymLink(homeUri, targetUri, onlyRead, user.getLogin());
    }

    /**
     * Delete a symbolic link.
     *
     * @param homeUri
     *            the URI of the home folder, where the link should be deleted
     * @return true, if deletion was successful
     * @throws IOException
     *             if get of module fails
     */
    public boolean deleteSymLink(URI homeUri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.deleteSymLink(homeUri);
    }

    private FileManagementInterface getFileManagementModule() throws IOException {
        KitodoServiceLoader<FileManagementInterface> loader = new KitodoServiceLoader<>(FileManagementInterface.class);
        return loader.loadModule();
    }
}
