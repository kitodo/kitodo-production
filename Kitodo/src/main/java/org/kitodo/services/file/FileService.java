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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.io.BackupFileRotation;
import org.hibernate.Hibernate;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.data.database.helper.enums.MetadataFormat;
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

    // program options initialized to default values
    private static final int BUFFER_SIZE = 4 * 1024;

    private static final String TEMPORARY_FILENAME_PREFIX = "temporary_";

    private static final ServiceManager serviceManager = new ServiceManager();

    public void createDirectory(URI parentFolderUri, String directoryName) throws IOException {
        if (!new File(parentFolderUri + directoryName).exists()) {
            ShellScript createDirScript = new ShellScript(new File(ConfigCore.getParameter("script_createDirMeta")));
            createDirScript.run(Arrays.asList(new String[] {parentFolderUri + directoryName }));
        }

    }

    /**
     * Creates a directory with a name given and assigns permissions to the
     * given user. Under Linux a script is used to set the file system
     * permissions accordingly. This cannot be done from within java code before
     * version 1.7.
     *
     * @param dirName
     *            Name of directory to create
     * @throws InterruptedException
     *             If the thread running the script is interrupted by another
     *             thread while it is waiting, then the wait is ended and an
     *             InterruptedException is thrown.
     * @throws IOException
     *             If an I/O error occurs.
     */

    public void createDirectoryForUser(String dirName, String userName) throws IOException, InterruptedException {
        if (!new File(dirName).exists()) {
            ShellScript createDirScript = new ShellScript(
                    new File(ConfigCore.getParameter("script_createDirUserHome")));
            createDirScript.run(Arrays.asList(userName, dirName));
        }
    }

    /**
     * This function implements file renaming. Renaming of files is full of
     * mischief under Windows which unaccountably holds locks on files.
     * Sometimes running the JVM’s garbage collector puts things right.
     *
     * @param fileUri
     *            File to move or rename
     * @param newFileName
     *            New file name / destination
     * @throws IOException
     *             is thrown if the rename fails permanently
     * @throws FileNotFoundException
     *             is thrown if old file (source file of renaming) does not
     *             exists
     */
    public void renameFile(URI fileUri, String newFileName) throws IOException {
        final int SLEEP_INTERVAL_MILLIS = 20;
        final int MAX_WAIT_MILLIS = 150000; // 2½ minutes
        File oldFile;
        File newFile;
        boolean success;
        int millisWaited = 0;

        if ((fileUri == null) || (newFileName == null)) {
            return;
        }

        oldFile = new File(fileUri);
        newFile = new File(newFileName);

        if (!oldFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("File " + fileUri + " does not exist for renaming.");
            }
            throw new FileNotFoundException(fileUri + " does not exist for renaming.");
        }

        if (newFile.exists()) {
            String message = "Renaming of " + fileUri + " into " + newFileName + " failed: Destination exists.";
            logger.error(message);
            throw new IOException(message);
        }

        do {
            if (SystemUtils.IS_OS_WINDOWS && millisWaited == SLEEP_INTERVAL_MILLIS) {
                logger.warn("Renaming " + fileUri
                        + " failed. This is Windows. Running the garbage collector may yield good results. "
                        + "Forcing immediate garbage collection now!");
                System.gc();
            }
            success = oldFile.renameTo(newFile);
            if (!success) {
                if (millisWaited == 0 && logger.isInfoEnabled()) {
                    logger.info("Renaming " + fileUri + " failed. File may be locked. Retrying...");
                }
                try {
                    Thread.sleep(SLEEP_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                }
                millisWaited += SLEEP_INTERVAL_MILLIS;
            }
        } while (!success && millisWaited < MAX_WAIT_MILLIS);

        if (!success) {
            logger.error("Rename " + fileUri + " failed. This is a permanent error. Giving up.");
            throw new IOException("Renaming of " + fileUri + " into " + newFileName + " failed.");
        }

        if (millisWaited > 0 && logger.isInfoEnabled()) {
            logger.info("Rename finally succeeded after" + Integer.toString(millisWaited) + " milliseconds.");
        }
    }

    /**
     * calculate all files with given file extension at specified directory
     * recursively.
     *
     * @param directory
     *            the directory to run through
     * @return number of files as Integer
     */
    public Integer getNumberOfFiles(File directory) {
        int count = 0;
        if (directory.isDirectory()) {
            /*
             * die Images zählen
             */
            count = list(Helper.imageNameFilter, directory).length;

            /*
             * die Unterverzeichnisse durchlaufen
             */
            String[] children = list(directory);
            for (int i = 0; i < children.length; i++) {
                count += getNumberOfFiles(new File(directory, children[i]));
            }
        }
        return count;
    }

    public Integer getNumberOfFiles(String inDir) {
        return getNumberOfFiles(new File(inDir));
    }

    /**
     * Copy directory.
     *
     * @param sourceDirectory
     *            source file
     * @param targetDirectory
     *            destination file
     * @return Long
     */
    public void copyDir(File sourceDirectory, File targetDirectory) throws IOException {
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        FileUtils.copyDirectory(sourceDirectory, targetDirectory, false);
    }

    public void copyFile(File srcFile, File destFile) throws IOException {
        FileUtils.copyFile(srcFile, destFile);
    }

    public void copyFileToDirectory(File sourceDirectory, File targetDirectory) throws IOException {
        FileUtils.copyFileToDirectory(sourceDirectory, targetDirectory);
    }

    public OutputStream write(URI uri) throws IOException, URISyntaxException {
        return new FileOutputStream(new File(mapUriToKitodoUri(uri)));
    }

    public InputStream read(URI uri) throws IOException {
        URL url = mapUriToKitodoUri(uri).toURL();
        return url.openStream();
    }

    public boolean delete(URI uri) throws IOException {
        File file = new File(uri);
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
            return true;
        }
        return false;
    }

    public boolean fileExist(URI uri) {
        return new File(uri).exists();
    }

    public OutputStream writeOrCreate(URI uri) throws IOException, URISyntaxException {
        return write(uri);
    }

    public void moveFile(File sourceDirectory, File targetDirectory) throws IOException {
        FileUtils.moveFile(sourceDirectory, targetDirectory);
    }

    public void moveDirectory(File sourceDirectory, File targetDirectory) throws IOException {
        FileUtils.moveDirectory(sourceDirectory, targetDirectory);
    }

    public String[] list(File file) {
        String[] unchecked = file.list();
        return unchecked != null ? unchecked : new String[0];
    }

    public String[] list(FilenameFilter filter, File file) {
        String[] unchecked = file.list(filter);
        return unchecked != null ? unchecked : new String[0];
    }

    public File[] listFiles(File file) {
        File[] unchecked = file.listFiles();
        return unchecked != null ? unchecked : new File[0];
    }

    public File[] listFiles(FileFilter filter, File file) {
        File[] unchecked = file.listFiles(filter);
        return unchecked != null ? unchecked : new File[0];
    }

    public File[] listFiles(FilenameFilter filter, File file) {
        File[] unchecked = file.listFiles(filter);
        return unchecked != null ? unchecked : new File[0];
    }

    public List<File> getCurrentFiles(File file) {
        File[] result = file.listFiles();
        if (result != null) {
            return Arrays.asList(result);
        } else {
            return Collections.emptyList();
        }
    }

    public List<File> getFilesByFilter(File file, FilenameFilter filter) {
        File[] result = file.listFiles(filter);
        if (result != null) {
            return Arrays.asList(result);
        } else {
            return Collections.emptyList();
        }
    }

    public void writeMetadataFile(Fileformat gdzfile, Process process) throws IOException, PreferencesException,
            InterruptedException, DAOException, SwapException, WriteException, URISyntaxException {
        OutputStream write = serviceManager.getFileService().write(process.getProcessBaseUri());

        RulesetService rulesetService = new RulesetService();
        boolean backupCondition;
        boolean writeResult;
        File temporaryMetadataFile;
        Fileformat ff;
        URI metadataFileUri;
        String temporaryMetadataFileName;

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
        temporaryMetadataFileName = getTemporaryMetadataFileName(metadataFileUri);

        ff.setDigitalDocument(gdzfile.getDigitalDocument());
        // ff.write(getMetadataFilePath());
        writeResult = ff.write(temporaryMetadataFileName);
        temporaryMetadataFile = new File(temporaryMetadataFileName);
        backupCondition = writeResult && temporaryMetadataFile.exists() && (temporaryMetadataFile.length() > 0);
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
    private void createBackupFile(Process process)
            throws IOException, InterruptedException, SwapException, DAOException, URISyntaxException {
        int numberOfBackups = 0;

        if (ConfigCore.getIntParameter("numberOfMetaBackups") != 0) {
            numberOfBackups = ConfigCore.getIntParameter("numberOfMetaBackups");
        }

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

    public URI getMetadataFilePath(Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        return getProcessSubTypeURI(process, ProcessSubType.META_XML, null);
    }

    private String getTemporaryMetadataFileName(URI fileName) {

        File temporaryFile = new File(fileName);
        String directoryPath = temporaryFile.getParentFile().getPath();
        String temporaryFileName = TEMPORARY_FILENAME_PREFIX + temporaryFile.getName();

        return directoryPath + File.separator + temporaryFileName;
    }

    /**
     * This method is needed for migraion purposes. It mappes existing
     * filePathes to the Correct URI.
     *
     * @param process
     *            the process, the uri is needed for.
     * @return the URI.
     */
    public URI getProcessBaseUriForExistingProcess(Process process) {
        String pfad = process.getId() + File.separator;
        pfad = pfad.replaceAll(" ", "__");
        return URI.create(pfad);
    }

    /**
     * Get's the URI for a Process Sub-location. Possible Locations are listed
     * in ProcessSubType
     *
     * @param process
     *            the process to get the sublocation for.
     * @param processSubType
     *            The subType.
     * @param id
     *            the id of the single object (e.g. image) if null, the root
     *            folder of the sublocation is returned
     * @return The URI of the requested location
     */
    public URI getProcessSubTypeURI(Process process, ProcessSubType processSubType, String id) {

        URI processDataDirectory = serviceManager.getProcessService().getProcessDataDirectory(process);

        if (id == null) {
            id = "";
        }

        switch (processSubType) {
            case IMAGE:
                return URI.create(processDataDirectory + "image" + File.separator + id);
            case IMAGE_SOURCE:
                return URI.create(getSourceDirectory(process) + id);
            case META_XML:
                return URI.create(processDataDirectory + "meta.xml");
            case TEMPLATE:
                return URI.create(processDataDirectory + "template.xml");
            case IMPORT:
                return URI.create(processDataDirectory + "import" + File.separator + id);
            case OCR:
                return URI.create(processDataDirectory + "ocr" + File.separator);
            case OCR_PDF:
                return URI.create(processDataDirectory + "ocr" + File.separator + process.getTitle() + "_pdf"
                        + File.separator + id);
            case OCR_TXT:
                return URI.create(processDataDirectory + "ocr" + File.separator + process.getTitle() + "_txt"
                        + File.separator + id);
            case OCR_WORD:
                return URI.create(processDataDirectory + "ocr" + File.separator + process.getTitle() + "_wc"
                        + File.separator + id);
            case OCR_ALTO:
                return URI.create(processDataDirectory + "ocr" + File.separator + process.getTitle() + "_alto"
                        + File.separator + id);
            default:
                return processDataDirectory;
        }

    }

    /**
     * deletes all process directorys and their content.
     *
     * @param process
     *            the processt o delete the doirectorys for.
     * @return true, if deletion was successfull.
     */
    public boolean deleteProcessContent(Process process) {
        for (ProcessSubType processSubType : ProcessSubType.values()) {
            URI processSubTypeURI = getProcessSubTypeURI(process, processSubType, null);
            try {
                delete(processSubTypeURI);
            } catch (IOException e) {
                logger.warn("uri " + processSubTypeURI + " could not be deleted");
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the image source directory
     *
     * @param process
     *            the process, to get the source directory for
     * @return the source directory as a string
     */
    public String getSourceDirectory(Process process) {
        File dir = new File(getProcessSubTypeURI(process, ProcessSubType.IMAGE, null));
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + "source"));
            }
        };
        File sourceFolder = null;
        String[] verzeichnisse = list(filterVerz, dir);
        if (verzeichnisse == null || verzeichnisse.length == 0) {
            sourceFolder = new File(dir, process.getTitle() + "_source");
            if (ConfigCore.getBooleanParameter("createSourceFolder", false)) {
                sourceFolder.mkdir();
            }
        } else {
            sourceFolder = new File(dir, verzeichnisse[0]);
        }

        return sourceFolder.getAbsolutePath();
    }

    private URI mapUriToKitodoUri(URI uri) throws MalformedURLException {
        return URI.create(ConfigCore.getKitodoDataDirectory() + uri);
    }

    /**
     * gets all sub URIs of an uri.
     *
     * @param processSubTypeURI
     *            the uri, to get the subUris from.
     * @return A List of sub uris.
     */
    public ArrayList<URI> getSubUris(URI processSubTypeURI) {
        ArrayList<URI> resultList = new ArrayList<>();
        File[] files = listFiles(new File(processSubTypeURI));
        for (File file : files) {
            resultList.add(file.toURI());
        }

        return resultList;
    }
}
