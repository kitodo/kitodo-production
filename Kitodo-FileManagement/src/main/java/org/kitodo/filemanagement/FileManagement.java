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

package org.kitodo.filemanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.config.Config;

public class FileManagement implements FileManagementInterface {

    private static final Logger logger = LogManager.getLogger(FileManagement.class);
    private static final FileMapper fileMapper = new FileMapper();

    @Override
    public URI create(URI parentFolderUri, String name, boolean file) throws IOException {
        if (file) {
            return createResource(parentFolderUri, name);
        }
        return createDirectory(parentFolderUri, name);
    }

    private URI createDirectory(URI parentFolderUri, String directoryName) throws IOException {
        parentFolderUri = fileMapper.mapAccordingToMappingType(parentFolderUri);
        File directory = new File(Paths.get(new File(parentFolderUri).getPath(), directoryName).toUri());
        if (!directory.exists() && !directory.mkdir()) {
            throw new IOException("Could not create directory.");
        }
        return fileMapper.unmapAccordingToMappingType(Paths.get(directory.getPath()).toUri());
    }

    private URI createResource(URI targetFolder, String fileName) throws IOException {
        targetFolder = fileMapper.mapAccordingToMappingType(targetFolder);
        File file = new File(Paths.get(new File(targetFolder).getPath(), fileName).toUri());
        if (file.exists() || file.createNewFile()) {
            return fileMapper.unmapAccordingToMappingType(Paths.get(file.getPath()).toUri());
        }
        return URI.create("");
    }

    @Override
    public OutputStream write(URI uri) throws IOException {
        uri = fileMapper.mapAccordingToMappingType(uri);
        return new FileOutputStream(new File(uri));
    }

    @Override
    public InputStream read(URI uri) throws IOException {
        uri = fileMapper.mapAccordingToMappingType(uri);
        URL url = uri.toURL();
        return url.openStream();
    }

    @Override
    public void copy(URI sourceUri, URI targetUri) throws IOException {
        sourceUri = fileMapper.mapAccordingToMappingType(sourceUri);
        targetUri = fileMapper.mapAccordingToMappingType(targetUri);
        if (!fileExist(sourceUri)) {
            throw new FileNotFoundException();
        } else if (isFile(sourceUri) && targetUri.getPath().contains(".")) {
            copyFile(new File(sourceUri), new File(targetUri));
        } else if (isFile(sourceUri)) {
            copyFileToDirectory(new File(sourceUri), new File(targetUri));
        } else if (isDirectory(sourceUri)) {
            copyDirectory(new File(sourceUri), new File(targetUri));
        }
    }

    private void copyDirectory(File sourceDirectory, File targetDirectory) throws IOException {
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        FileUtils.copyDirectory(sourceDirectory, targetDirectory, false);
    }

    private void copyFile(File sourceFile, File destinationFile) throws IOException {
        FileUtils.copyFile(sourceFile, destinationFile);
    }

    private void copyFileToDirectory(File sourceFile, File targetDirectory) throws IOException {
        FileUtils.copyFileToDirectory(sourceFile, targetDirectory);
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        uri = fileMapper.mapAccordingToMappingType(uri);
        File file = new File(uri);
        if (file.exists()) {
            if (file.isFile()) {
                return file.delete();
            }
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void move(URI sourceUri, URI targetUri) throws IOException {
        copy(sourceUri, targetUri);
        delete(sourceUri);
    }

    @Override
    public URI rename(URI uri, String newName) throws IOException {
        final int SLEEP_INTERVAL_MILLIS = 20;
        final int MAX_WAIT_MILLIS = 150000; // 2½ minutes
        int millisWaited = 0;

        if ((uri == null) || (newName == null)) {
            return null;
        }

        String substring = uri.toString().substring(0, uri.toString().lastIndexOf('/') + 1);
        URI newFileUri = URI.create(substring + newName);
        URI mappedFileURI = fileMapper.mapAccordingToMappingType(uri);
        URI mappedNewFileURI = fileMapper.mapAccordingToMappingType(newFileUri);
        boolean success;

        if (!fileExist(mappedFileURI)) {
            if (logger.isDebugEnabled()) {
                logger.debug("File " + uri.getPath() + " does not exist for renaming.");
            }
            throw new FileNotFoundException(uri + " does not exist for renaming.");
        }

        if (fileExist(mappedNewFileURI)) {
            String message = "Renaming of " + uri + " into " + newName + " failed: Destination exists.";
            logger.error(message);
            throw new IOException(message);
        }

        File fileToRename = new File(mappedFileURI);
        File renamedFile = new File(mappedNewFileURI);
        do {
            if (SystemUtils.IS_OS_WINDOWS && millisWaited == SLEEP_INTERVAL_MILLIS) {
                logger.warn("Renaming " + uri
                        + " failed. This is Windows. Running the garbage collector may yield good results. "
                        + "Forcing immediate garbage collection now!");
                System.gc();
            }
            success = fileToRename.renameTo(renamedFile);
            if (!success) {
                if (millisWaited == 0 && logger.isInfoEnabled()) {
                    logger.info("Renaming " + uri + " failed. File may be locked. Retrying...");
                }
                try {
                    Thread.sleep(SLEEP_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    logger.warn("The thread was interrupted");
                }
                millisWaited += SLEEP_INTERVAL_MILLIS;
            }
        } while (!success && millisWaited < MAX_WAIT_MILLIS);

        if (!success) {
            logger.error("Rename " + uri + " failed. This is a permanent error. Giving up.");
            throw new IOException("Renaming of " + uri + " into " + newName + " failed.");
        }

        if (millisWaited > 0 && logger.isInfoEnabled()) {
            logger.info("Rename finally succeeded after" + Integer.toString(millisWaited) + " milliseconds.");
        }
        return fileMapper.unmapAccordingToMappingType(Paths.get(renamedFile.getPath()).toUri());
    }

    @Override
    public boolean fileExist(URI uri) {
        uri = fileMapper.mapAccordingToMappingType(uri);
        return new File(uri).exists();
    }

    @Override
    public boolean isFile(URI uri) {
        uri = fileMapper.mapAccordingToMappingType(uri);
        return new File(uri).isFile();
    }

    @Override
    public boolean isDirectory(URI directory) {
        directory = fileMapper.mapAccordingToMappingType(directory);
        return new File(directory).isDirectory();
    }

    @Override
    public boolean canRead(URI uri) {
        uri = fileMapper.mapAccordingToMappingType(uri);
        return new File(uri).canRead();
    }

    @Override
    public Integer getNumberOfFiles(FilenameFilter filter, URI directory) {
        int count = 0;
        directory = fileMapper.mapAccordingToMappingType(directory);
        if (filter == null) {
            count += iterateOverDirectories(directory);
        } else {
            count += iterateOverSpecificDirectories(filter, directory);
        }
        return count;
    }

    /**
     * Iterate over children directories of directory.
     *
     * @param directory
     *            as URI
     * @return amount of files
     */
    private Integer iterateOverDirectories(URI directory) {
        int count = 0;
        if (isDirectory(directory)) {
            ArrayList<URI> children = getSubUris(null, directory);
            for (URI child : children) {
                child = fileMapper.mapAccordingToMappingType(child);
                if (isDirectory(child)) {
                    count += getNumberOfFiles(null, child);
                } else {
                    count += 1;
                }
            }
        }
        return count;
    }

    /**
     * Iterate over children specific directories of directory.
     *
     * @param directory
     *            as URI
     * @return amount of specific (eg. image) files
     */
    private Integer iterateOverSpecificDirectories(FilenameFilter filter, URI directory) {
        int count = 0;
        if (isDirectory(directory)) {
            count = getSubUris(filter, directory).size();
            ArrayList<URI> children = getSubUris(null, directory);
            for (URI child : children) {
                child = fileMapper.mapAccordingToMappingType(child);
                count += getNumberOfFiles(filter, child);
            }
        }
        return count;
    }

    @Override
    public String getFileNameWithExtension(URI uri) {
        return FilenameUtils.getName(uri.getPath());
    }

    @Override
    public ArrayList<URI> getSubUris(FilenameFilter filter, URI uri) {
        if (!uri.isAbsolute()) {
            uri = fileMapper.mapAccordingToMappingType(uri);
        }
        ArrayList<URI> resultList = new ArrayList<>();
        File[] files;
        if (filter == null) {
            files = listFiles(new File(uri));
        } else {
            files = listFiles(filter, new File(uri));
        }
        for (File file : files) {
            URI tempURI = Paths.get(file.getPath()).toUri();
            resultList.add(fileMapper.unmapAccordingToMappingType(tempURI));
        }
        return resultList;
    }

    /**
     * Lists all Files at the given path.
     *
     * @param file
     *            the Directory to get the Files from
     * @return an Array of Files
     */
    private File[] listFiles(File file) {
        File[] unchecked = file.listFiles();
        return unchecked != null ? unchecked : new File[0];
    }

    /**
     * Lists all files at the given path and with a given filter.
     *
     * @param file
     *            the directory to get the Files from
     * @return an Array of Files
     */
    private File[] listFiles(FilenameFilter filter, File file) {
        File[] unchecked = file.listFiles(filter);
        return unchecked != null ? unchecked : new File[0];
    }

    @Override
    public URI createProcessLocation(String processId) throws IOException {
        File processRootDirectory = new File(
                (Config.getKitodoDataDirectory() + File.separator + processId));
        if (!processRootDirectory.exists() && !processRootDirectory.mkdir()) {
            throw new IOException("Could not create processRoot directory.");
        }
        return fileMapper.unmapAccordingToMappingType(Paths.get(processRootDirectory.getPath()).toUri());
    }

    @Override
    public URI createUriForExistingProcess(String processId) {
        return null;
    }

    public URI getProcessSubTypeUri(URI processBaseUri, ProcessSubType subType, int id) {
        return null;
    }

    @Override
    public boolean createSymLink(URI targetUri, URI homeUri, boolean onlyRead, String userLogin) {
        File imagePath = new File(fileMapper.mapAccordingToMappingType(homeUri));
        File userHome = new File(getDecodedPath(targetUri));
        if (userHome.exists()) {
            return false;
        }
        String command = Config.getParameter("script_createSymLink") + " ";
        command += imagePath + " " + userHome + " ";
        if (onlyRead) {
            command += Config.getParameter("UserForImageReading", "root");
        } else {
            command += userLogin;
        }
        try {
            ShellScript.legacyCallShell(command);
            return true;
        } catch (IOException e) {
            logger.error("IOException downloadToHome()", e);
            return false;
        }
    }

    @Override
    public boolean deleteSymLink(URI homeUri) {
        String command = Config.getParameter("script_deleteSymLink");
        ShellScript deleteSymLinkScript;
        try {
            deleteSymLinkScript = new ShellScript(new File(command));
            deleteSymLinkScript.run(Collections.singletonList(new File(getDecodedPath(homeUri)).getPath()));
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException in deleteSymLink()", e);
            return false;
        } catch (IOException e) {
            logger.error("IOException in deleteSymLink()", e);
            return false;
        }
        return true;
    }

    private String getDecodedPath(URI uri) {
        uri = fileMapper.mapAccordingToMappingType(uri);
        String uriToDecode = new File(uri).getPath();
        String decodedPath;
        try {
            decodedPath = URLDecoder.decode(uriToDecode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            return "";
        }
        return decodedPath;
    }
}
