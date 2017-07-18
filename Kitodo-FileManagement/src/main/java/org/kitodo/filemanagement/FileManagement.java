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
    public URI create(URI parentFolderUri, String directoryName, boolean file) throws IOException {
        File directory = new File(parentFolderUri.getPath() + File.separator + directoryName);
        if (!directory.mkdir()) {
            throw new IOException("Could not create directory.");
        }
        return fileMapper.unmapAccordingToMappingType(Paths.get(directory.getPath()).toUri());
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
    public void copy(URI sourceDirectory, URI targetDirectory) throws IOException {
        sourceDirectory = fileMapper.mapAccordingToMappingType(sourceDirectory);
        targetDirectory = fileMapper.mapAccordingToMappingType(targetDirectory);
        copyDirectory(new File(sourceDirectory), new File(targetDirectory));
    }

    private void copyDirectory(File sourceDirectory, File targetDirectory) throws IOException {
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        FileUtils.copyDirectory(sourceDirectory, targetDirectory, false);
    }

    private void copyFile(URI sourceFile, URI destinationFile) throws IOException {
        File srcFile = new File(fileMapper.mapAccordingToMappingType(sourceFile));
        File destFile = new File(fileMapper.mapAccordingToMappingType(destinationFile));
        FileUtils.copyFile(srcFile, destFile);
    }

    private void copyFileToDirectory(URI sourceFile, URI targetDirectory) throws IOException {
        File file = new File(fileMapper.mapAccordingToMappingType(sourceFile));
        File directory = new File(fileMapper.mapAccordingToMappingType(targetDirectory));
        FileUtils.copyFileToDirectory(file, directory);
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        uri = fileMapper.mapAccordingToMappingType(uri);
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
        return 0;
    }

    @Override
    public String getFileNameWithExtension(URI uri) {
        return FilenameUtils.getName(uri.getPath());
    }

    public ArrayList<URI> getSubUris(FilenameFilter filter, URI uri) {
        return new ArrayList<>();
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
