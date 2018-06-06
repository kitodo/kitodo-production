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

package de.sub.goobi.helper;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.export.download.TiffHeader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.filters.FileNameEndsWithFilter;
import org.kitodo.config.Config;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class WebDav implements Serializable {
    private final ServiceManager serviceManager = new ServiceManager();
    private final FileService fileService = new FileService();
    private static final long serialVersionUID = -1929234096626965538L;
    private static final Logger logger = LogManager.getLogger(WebDav.class);
    private static final String ERROR_UPLOADING = "errorUploading";

    /*
     * Kopieren bzw. symbolische Links für einen Prozess in das Benutzerhome
     */
    private String doneDirectoryName;

    public WebDav() {
        doneDirectoryName = ConfigCore.getParameter("doneDirectoryName", "fertig/");
    }

    /**
     * Retrieve all folders from one directory.
     */
    public List<URI> uploadAllFromHome(String inVerzeichnis) {
        User currentUser = serviceManager.getUserService().getAuthenticatedUser();
        List<URI> files = new ArrayList<>();
        FilenameFilter filter = new FileNameEndsWithFilter("]");

        try {
            URI directoryName = serviceManager.getUserService().getHomeDirectory(currentUser).resolve(inVerzeichnis);
            files = fileService.getSubUris(filter, directoryName);
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_UPLOADING, new Object[] {"Home"}, logger, e);
            return files;
        }

        for (URI data : files) {
            String dataString = data.toString();
            if (dataString.endsWith("/") || dataString.endsWith("\\")) {
                data = URI.create(dataString.substring(0, dataString.length() - 1));
            }
            if (data.toString().contains("/")) {
                // TODO: check what happens here
                data = URI.create(dataString.substring(dataString.lastIndexOf('/')));
            }
        }
        return files;

    }

    /**
     * Remove Folders from Directory.
     *
     * @param uris
     *            list of URI
     * @param directory
     *            URI
     */
    public void removeAllFromHome(List<URI> uris, URI directory) {
        URI verzeichnisAlle;
        User currentUser = serviceManager.getUserService().getAuthenticatedUser();
        try {
            verzeichnisAlle = serviceManager.getUserService().getHomeDirectory(currentUser).resolve(directory);
            for (URI name : uris) {
                fileService.deleteSymLink(verzeichnisAlle.resolve(name));
            }
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_UPLOADING, new Object[] {"Home"}, logger, e);
        }
    }

    /**
     * Upload from home.
     *
     * @param process
     *            Process object
     */
    public void uploadFromHome(Process process) {
        User currentUser = serviceManager.getUserService().getAuthenticatedUser();
        uploadFromHome(currentUser, process);
    }

    /**
     * Upload from home.
     *
     * @param user
     *            User object
     * @param process
     *            Process object
     */
    public void uploadFromHome(User user, Process process) {
        URI destination;
        try {
            destination = serviceManager.getUserService().getHomeDirectory(user);
            if (user.isWithMassDownload()) {
                destination = Paths.get(new File(destination).getPath(), process.getProject().getTitle()).toUri();
                destination = Paths.get(new File(destination).getPath().replaceAll(" ", "__")).toUri();
                if (!fileService.fileExist(destination)
                        && !fileService.isDirectory(fileService.createResource(destination.toString()))) {
                    String param = new File(destination).getPath().replaceAll(" ", "__");
                    Helper.setErrorMessage("MassDownloadProjectCreationError", new Object[] {param});
                    logger.error("Can not create project directory "
                            + Paths.get(new File(destination).getPath().replaceAll(" ", "__")).toUri());
                    return;
                }
                destination = Paths.get(new File(destination).getPath(), getEncodedProcessLinkName(process)).toUri();
                fileService.deleteSymLink((destination));
            }
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_UPLOADING, new Object[] {"Home"}, logger, e);
        }
    }

    /**
     * Download to home.
     *
     * @param process
     *            Process object
     * @param onlyRead
     *            boolean
     */
    public void downloadToHome(Process process, boolean onlyRead) {
        saveTiffHeader(process);
        User currentUser = serviceManager.getUserService().getAuthenticatedUser();
        URI source;
        URI userHome;

        try {
            source = serviceManager.getFileService().getImagesDirectory(process);
            userHome = serviceManager.getUserService().getHomeDirectory(currentUser);

            // for mass download, the project and directory must exist
            if (currentUser.isWithMassDownload()) {
                URI project = Paths.get(userHome + process.getProject().getTitle()).toUri();
                fileService.createDirectoryForUser(project, currentUser.getLogin());

                project = Paths.get(userHome + doneDirectoryName).toUri();
                fileService.createDirectoryForUser(project, currentUser.getLogin());
            }

            URI destination = userHome;
            if (currentUser.isWithMassDownload() && process.getProject() != null) {
                destination = Paths.get(new File(destination).getPath(), process.getProject().getTitle()).toUri();
            }
            destination = Paths.get(new File(destination).getPath(), getEncodedProcessLinkName(process)).toUri();

            fileService.createSymLink(source, destination, onlyRead, currentUser);
        } catch (IOException e) {
            Helper.setErrorMessage("errorDownloading", new Object[] {"Home"}, logger, e);
        }
    }

    /**
     * Method creates process link name and next encodes it for URI creation.
     *
     * @param process
     *            object
     * @return encoded process link name
     */
    private String getEncodedProcessLinkName(Process process) {
        String processLinkName = process.getTitle() + "__[" + process.getId() + "]";
        String encodedProcessLinkName;
        try {
            encodedProcessLinkName = URLEncoder.encode(processLinkName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
        return encodedProcessLinkName;
    }

    /**
     * Method checks if tiff header already exists. If yes method breaks, if not
     * method creates it and saves to it.
     *
     * @param inProcess
     *            process object
     */
    private void saveTiffHeader(Process inProcess) {
        try {
            URI imagesDirectory = serviceManager.getFileService().getImagesDirectory(inProcess);
            String path = Config.getKitodoDataDirectory() + imagesDirectory;
            URI tiffWriterURI = Paths.get(new File(path).getAbsolutePath(), "tiffwriter.conf").toUri();
            if (new File(tiffWriterURI).exists()) {
                return;
            }
            TiffHeader tif = new TiffHeader(inProcess);
            try (BufferedWriter outfile = new BufferedWriter(
                    new OutputStreamWriter(fileService.write(tiffWriterURI), StandardCharsets.UTF_8))) {
                outfile.write(tif.getTiffAlles());
            }
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage("errorDownloading", new Object[] {"Home"}, logger, e);
        }
    }
}
