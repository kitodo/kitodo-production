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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.command.CommandInterface;
import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.filemanagement.filters.FileNameEndsWithFilter;
import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterFileManagement;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class FileManagement implements FileManagementInterface {

    private static final Logger logger = LogManager.getLogger(FileManagement.class);
    private static final FileMapper fileMapper = new FileMapper();

    private static final String IMAGES_DIRECTORY_NAME = "images";

    private final CommandInterface commandService = new KitodoServiceLoader<CommandInterface>(CommandInterface.class)
            .loadModule();
    @Override
    public URI create(URI parentFolderUri, String name, boolean file) throws IOException {
        if (file) {
            return createResource(parentFolderUri, name);
        }
        return createDirectory(parentFolderUri, name);
    }

    private URI createDirectory(URI parentFolderUri, String directoryName) throws IOException {
        parentFolderUri = fileMapper.mapUriToKitodoDataDirectoryUri(parentFolderUri);
        File directory = new File(Paths.get(new File(parentFolderUri).getPath(), directoryName).toUri());
        if (!directory.exists() && !directory.mkdir()) {
            throw new IOException("Could not create directory: " + directory);
        }
        return fileMapper.unmapUriFromKitodoDataDirectoryUri(Paths.get(directory.getPath()).toUri());
    }

    private URI createResource(URI targetFolder, String fileName) throws IOException {
        targetFolder = fileMapper.mapUriToKitodoDataDirectoryUri(targetFolder);
        File file = new File(Paths.get(new File(targetFolder).getPath(), fileName).toUri());
        if (file.exists() || file.createNewFile()) {
            return fileMapper.unmapUriFromKitodoDataDirectoryUri(Paths.get(file.getPath()).toUri());
        }
        return URI.create("");
    }

    @Override
    public OutputStream write(URI uri) throws IOException {
        uri = fileMapper.mapUriToKitodoDataDirectoryUri(uri);
        return Files.newOutputStream(Paths.get(uri));
    }

    @Override
    public InputStream read(URI uri) throws IOException {
        uri = fileMapper.mapUriToKitodoDataDirectoryUri(uri);
        return uri.toURL().openStream();
    }

    @Override
    public void copy(URI sourceUri, URI targetUri) throws IOException {
        sourceUri = fileMapper.mapUriToKitodoDataDirectoryUri(sourceUri);
        boolean isDirectory = targetUri.getPath().endsWith("/");
        targetUri = fileMapper.mapUriToKitodoDataDirectoryUri(targetUri);
        String targetPath = targetUri.getPath();
        File targetFile = new File(targetPath);
        if (!fileExist(sourceUri)) {
            throw new FileNotFoundException();
        } else if (isFile(sourceUri) && ((targetFile.exists() && !targetFile.isDirectory()) || !isDirectory)) {
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
        if (Objects.isNull(uri) || uri.getPath().isEmpty()) {
            /*
                This exception is thrown when the passed URI is empty or null.
                Using this URI would cause the deletion of the metadata directory.
            */
            throw new IOException("Attempt to delete subdirectory with URI that is empty or null!");
        }
        uri = fileMapper.mapUriToKitodoDataDirectoryUri(uri);
        File file = new File(uri);
        if (file.exists()) {
            if (file.isFile()) {
                return Files.deleteIfExists(file.toPath());
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
        if (Objects.isNull(uri) || Objects.isNull(newName)) {
            return null;
        }

        String substring = uri.toString().substring(0, uri.toString().lastIndexOf('/') + 1);
        if (newName.contains("/")) {
            newName = newName.substring(newName.lastIndexOf('/') + 1);
        }
        URI newFileUri = URI.create(substring + newName);
        URI mappedFileURI = fileMapper.mapUriToKitodoDataDirectoryUri(uri);
        URI mappedNewFileURI = fileMapper.mapUriToKitodoDataDirectoryUri(newFileUri);

        if (!fileExist(mappedFileURI)) {
            logger.debug("File {} does not exist for renaming.", uri.getPath());
            throw new FileNotFoundException(uri + " does not exist for renaming.");
        }

        if (fileExist(mappedNewFileURI)) {
            String message = "Renaming of " + uri + " into " + newName + " failed: Destination exists.";
            logger.error(message);
            throw new IOException(message);
        }

        return performRename(mappedFileURI, mappedNewFileURI);
    }

    private URI performRename(URI mappedFileURI, URI mappedNewFileURI) throws IOException {
        File fileToRename = new File(mappedFileURI);
        File renamedFile = new File(mappedNewFileURI);

        final int sleepIntervalMilliseconds = 20;
        final int maxWaitMilliseconds = KitodoConfig.getIntParameter(ParameterFileManagement.FILE_MAX_WAIT_MILLISECONDS);

        boolean success;
        int millisWaited = 0;

        do {
            if (SystemUtils.IS_OS_WINDOWS && millisWaited == sleepIntervalMilliseconds) {
                logger.warn("Renaming {} failed. This is Windows. Running the garbage collector may yield good"
                        + " results. Forcing immediate garbage collection now!",
                    fileToRename.getName());
                System.gc();
            }
            success = fileToRename.renameTo(renamedFile);
            if (!success) {
                if (millisWaited == 0) {
                    logger.info("Renaming {} failed. File may be locked. Retrying...", fileToRename.getName());
                }
                waitForThread(sleepIntervalMilliseconds);
                millisWaited += sleepIntervalMilliseconds;
            }
        } while (!success && millisWaited < maxWaitMilliseconds);

        if (!success) {
            logger.error("Rename {} failed. This is a permanent error. Giving up.", fileToRename.getName());
            throw new IOException(
                    "Renaming of " + fileToRename.getName() + " into " + renamedFile.getName() + " failed.");
        }

        if (millisWaited > 0) {
            logger.info("Rename finally succeeded after {} milliseconds.", Integer.toString(millisWaited));
        }
        return fileMapper.unmapUriFromKitodoDataDirectoryUri(Paths.get(renamedFile.getPath()).toUri());
    }

    private void waitForThread(int sleepIntervalMilliseconds) {
        try {
            Thread.sleep(sleepIntervalMilliseconds);
        } catch (InterruptedException e) {
            logger.warn("The thread was interrupted");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean fileExist(URI uri) {
        boolean exists = new File(fileMapper.mapUriToKitodoDataDirectoryUri(uri)).exists();
        logger.trace(exists ? "Found {}" : "No such file: {}", uri);
        return exists;
    }

    @Override
    public boolean isFile(URI uri) {
        uri = fileMapper.mapUriToKitodoDataDirectoryUri(uri);
        return new File(uri).isFile();
    }

    @Override
    public boolean isDirectory(URI directory) {
        directory = fileMapper.mapUriToKitodoDataDirectoryUri(directory);
        return new File(directory).isDirectory();
    }

    @Override
    public boolean canRead(URI uri) {
        uri = fileMapper.mapUriToKitodoDataDirectoryUri(uri);
        return new File(uri).canRead();
    }

    @Override
    public Integer getNumberOfFiles(FilenameFilter filter, URI directory) {
        int count = 0;
        directory = fileMapper.mapUriToKitodoDataDirectoryUri(directory);
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
            List<URI> children = getSubUris(null, directory);
            for (URI child : children) {
                child = fileMapper.mapUriToKitodoDataDirectoryUri(child);
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
            List<URI> children = getSubUris(null, directory);
            for (URI child : children) {
                child = fileMapper.mapUriToKitodoDataDirectoryUri(child);
                count += getNumberOfFiles(filter, child);
            }
        }
        return count;
    }

    @Override
    public Long getSizeOfDirectory(URI directory) throws IOException {
        if (!directory.isAbsolute()) {
            directory = fileMapper.mapUriToKitodoDataDirectoryUri(directory);
        }
        if (isDirectory(directory)) {
            return FileUtils.sizeOfDirectory(new File(directory));
        } else {
            throw new IOException("Given URI doesn't point to the directory!");
        }
    }

    @Override
    public String getFileNameWithExtension(URI uri) {
        return FilenameUtils.getName(uri.getPath());
    }

    @Override
    public List<URI> getSubUris(FilenameFilter filter, URI uri) {
        if (!uri.isAbsolute()) {
            uri = fileMapper.mapUriToKitodoDataDirectoryUri(uri);
        }
        List<URI> resultList = new ArrayList<>();
        File[] files;
        if (filter == null) {
            files = listFiles(new File(uri));
        } else {
            files = listFiles(filter, new File(uri));
        }
        for (File file : files) {
            URI tempURI = Paths.get(file.getPath()).toUri();
            resultList.add(fileMapper.unmapUriFromKitodoDataDirectoryUri(tempURI));
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
        File processRootDirectory = new File(KitodoConfig.getKitodoDataDirectory() + File.separator + processId);
        String scriptCreateDirMeta = KitodoConfig.getParameter("script_createDirMeta");
        String command = scriptCreateDirMeta + ' ' + processRootDirectory.getPath();
        if (!processRootDirectory.exists() && !commandService.runCommand(command.hashCode(), command).isSuccessful()) {
            throw new IOException("Could not create processRoot directory.");
        }
        return fileMapper.unmapUriFromKitodoDataDirectoryUri(Paths.get(processRootDirectory.getPath()).toUri());
    }

    @Override
    public URI createUriForExistingProcess(String processId) {
        return URI.create(processId);
    }

    @Override
    public URI getProcessSubTypeUri(URI processBaseUri, String processTitle, ProcessSubType subType,
            String resourceName) {
        return URI.create(getProcessSubType(processBaseUri.toString(), processTitle, subType, resourceName));
    }

    /**
     * Get part of URI specific for process and process sub type.
     *
     * @param processTitle
     *            tile of process
     * @param processSubType
     *            object
     * @param resourceName
     *            as String
     * @return process specific part of URI
     */
    private String getProcessSubType(String processID, String processTitle, ProcessSubType processSubType,
            String resourceName) {
        processTitle = encodeTitle(processTitle);
        final String ocr = "/ocr/";
        if (Objects.isNull(resourceName)) {
            resourceName = "";
        }

        switch (processSubType) {
            case IMAGE:
                return processID + "/" + IMAGES_DIRECTORY_NAME + "/" + resourceName;
            case IMAGE_SOURCE:
                return getSourceDirectory(processID, processTitle) + resourceName;
            case META_XML:
                return processID + "/meta.xml";
            case TEMPLATE:
                return processID + "/template.xml";
            case IMPORT:
                return processID + "/import/" + resourceName;
            case OCR:
                return processID + ocr;
            case OCR_PDF:
                return processID + ocr + processTitle + "_pdf/" + resourceName;
            case OCR_TXT:
                return processID + ocr + processTitle + "_txt/" + resourceName;
            case OCR_WORD:
                return processID + ocr + processTitle + "_wc/" + resourceName;
            case OCR_ALTO:
                return processID + ocr + processTitle + "_alto/" + resourceName;
            default:
                return "";
        }
    }

    /**
     * Remove possible white spaces from process titles.
     *
     * @param title
     *            process title
     * @return encoded process title
     */
    private String encodeTitle(String title) {
        if (title.contains(" ")) {
            title = title.replace(" ", "__");
        }
        return title;
    }

    /**
     * Gets the image source directory.
     *
     * @param processTitle
     *            title of the process, to get the source directory for
     * @return the source directory as a string
     */
    private URI getSourceDirectory(String processId, String processTitle) {
        final String suffix = "_" + KitodoConfig.getParameter(ParameterFileManagement.DIRECTORY_SUFFIX, "tif");
        URI dir = URI.create(getProcessSubType(processId, processTitle, ProcessSubType.IMAGE, null));
        FilenameFilter filterDirectory = new FileNameEndsWithFilter(suffix);
        URI sourceFolder = URI.create("");
        try {
            List<URI> directories = getSubUris(filterDirectory, dir);
            if (directories.isEmpty()) {
                sourceFolder = dir.resolve(processTitle + suffix + "/");
                if (KitodoConfig.getBooleanParameter(ParameterFileManagement.CREATE_SOURCE_FOLDER, false)) {
                    if (!fileExist(dir)) {
                        createDirectory(dir.resolve(".."), IMAGES_DIRECTORY_NAME);
                    }
                    createDirectory(dir, processTitle + suffix);
                }
            } else {
                sourceFolder = dir.resolve("/" + directories.get(0));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return sourceFolder;
    }

    @Override
    public boolean createSymLink(URI homeUri, URI targetUri, boolean onlyRead, String userLogin) {
        File imagePath = new File(fileMapper.mapUriToKitodoDataDirectoryUri(homeUri));
        File userHome = new File(getDecodedPath(targetUri));
        if (userHome.exists()) {
            return false;
        }

        String command = KitodoConfig.getParameter("script_createSymLink");
        CommandService commandService = new CommandService();
        List<String> parameters = new ArrayList<>();
        parameters.add(imagePath.getAbsolutePath());
        parameters.add(userHome.getAbsolutePath());

        if (onlyRead) {
            parameters.add(KitodoConfig.getParameter("UserForImageReading", "root"));
        } else {
            parameters.add(userLogin);
        }

        try {
            return commandService.runCommand(new File(command), parameters).isSuccessful();
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException in createSymLink", e);
            return false;
        } catch (IOException e) {
            logger.error("IOException in createSymLink", e);
            return false;
        }
    }

    @Override
    public boolean deleteSymLink(URI homeUri) {
        File homeFile = new File(fileMapper.mapUriToKitodoDataDirectoryUri(homeUri));

        String command = KitodoConfig.getParameter("script_deleteSymLink");
        CommandService commandService = new CommandService();
        List<String> parameters = new ArrayList<>();
        try {
            parameters.add(URLDecoder.decode(homeFile.getAbsolutePath(), StandardCharsets.UTF_8.name()));
            return commandService.runCommand(new File(command), parameters).isSuccessful();
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException in deleteSymLink", e);
            return false;
        } catch (IOException e) {
            logger.error("IOException in deleteSymLink", e);
            return false;
        }
    }

    private String getDecodedPath(URI uri) {
        uri = fileMapper.mapUriToKitodoDataDirectoryUri(uri);
        String uriToDecode = new File(uri).getPath();
        String decodedPath;
        try {
            decodedPath = URLDecoder.decode(uriToDecode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
        return decodedPath;
    }

    public File getFile(URI uri) {
        uri = fileMapper.mapUriToKitodoDataDirectoryUri(uri);
        return new File(uri);
    }
}
