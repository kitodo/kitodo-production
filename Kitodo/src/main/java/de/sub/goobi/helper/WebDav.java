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
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class WebDav implements Serializable {
    private final ServiceManager serviceManager = new ServiceManager();
    private final FileService fileService = new FileService();
    private static final long serialVersionUID = -1929234096626965538L;
    private static final Logger logger = LogManager.getLogger(WebDav.class);

    /*
     * Kopieren bzw. symbolische Links für einen Prozess in das Benutzerhome
     */

    private static String DONEDIRECTORYNAME = "fertig/";

    public WebDav() {
        DONEDIRECTORYNAME = ConfigCore.getParameter("doneDirectoryName", "fertig/");
    }

    /**
     * Retrieve all folders from one directory.
     */

    public List<URI> uploadAllFromHome(String inVerzeichnis) {
        User currentUser = Helper.getCurrentUser();
        ArrayList<URI> files = new ArrayList<>();
        FilenameFilter filter = new FileNameEndsWithFilter("]");

        try {
            if (currentUser != null) {
                URI directoryName = serviceManager.getUserService().getHomeDirectory(currentUser).resolve(inVerzeichnis);
                files = fileService.getSubUris(filter, directoryName);
            } else {
                Helper.setFehlerMeldung("uploadFromHomeAlle abgebrochen, Fehler - no user assigned");
                return files;
            }
        } catch (IOException ioe) {
            logger.error("Exception uploadFromHomeAlle()", ioe);
            Helper.setFehlerMeldung("uploadFromHomeAlle abgebrochen, Fehler", ioe.getMessage());
            return files;
        }

        for (URI data : files) {
            String dataString = data.toString();
            if (dataString.endsWith("/") || dataString.endsWith("\\")) {
                data = URI.create(dataString.substring(0, dataString.length() - 1));
            }
            if (data.toString().contains("/")) {
                //TODO: check what happens here
                data = URI.create(dataString.substring(dataString.lastIndexOf("/")));
            }
        }
        return files;

    }

    /**
     * Remove Folders from Directory.
     */
    // TODO: Use generic types
    public void removeAllFromHome(List<URI> inList, URI inVerzeichnis) {
        URI verzeichnisAlle;
        User currentUser = Helper.getCurrentUser();
        try {
            if (currentUser != null) {
                verzeichnisAlle = serviceManager.getUserService().getHomeDirectory(currentUser)
                        .resolve(inVerzeichnis);
                for (URI name : inList) {
                    fileService.deleteSymLink(verzeichnisAlle.resolve(name));
                }
            } else {
                Helper.setFehlerMeldung("Upload stopped, error - no logged user");
            }
        } catch (Exception ioe) {
            logger.error("Exception RemoveFromHomeAlle()", ioe);
            Helper.setFehlerMeldung("Upload stopped, error", ioe.getMessage());
        }
    }

    /**
     * Upload from home.
     *
     * @param process
     *            Process object
     */
    public void uploadFromHome(Process process) {
        User currentUser = Helper.getCurrentUser();
        if (currentUser != null) {
            uploadFromHome(currentUser, process);
        }
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
                    List<String> param = new ArrayList<>();
                    param.add(new File(destination).getPath().replaceAll(" ", "__"));
                    Helper.setFehlerMeldung(Helper.getTranslation("MassDownloadProjectCreationError", param));
                    logger.error("Can not create project directory "
                            + Paths.get(new File(destination).getPath().replaceAll(" ", "__")).toUri());
                    return;
                }
                destination = Paths.get(new File(destination).getPath(), getEncodedProcessLinkName(process)).toUri();
                fileService.deleteSymLink((destination));
            }
        } catch (IOException ioe) {
            logger.error("Exception uploadFromHome(...)", ioe);
            Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
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
        User currentUser = Helper.getCurrentUser();
        URI source;
        URI userHome;

        try {
            if (currentUser != null) {
                source = serviceManager.getFileService().getImagesDirectory(process);
                userHome = serviceManager.getUserService().getHomeDirectory(currentUser);

                /*
                 * bei Massendownload muss auch das Projekt- und Fertig-Verzeichnis
                 * existieren
                 */
                if (currentUser.isWithMassDownload()) {
                    URI project = Paths.get(userHome + process.getProject().getTitle()).toUri();
                    fileService.createDirectoryForUser(project, currentUser.getLogin());

                    project = Paths.get(userHome + DONEDIRECTORYNAME).toUri();
                    fileService.createDirectoryForUser(project, currentUser.getLogin());
                }

                URI destination = userHome;
                if (currentUser.isWithMassDownload() && process.getProject() != null) {
                    destination = Paths.get(new File(destination).getPath(), process.getProject().getTitle()).toUri();
                }
                destination = Paths.get(new File(destination).getPath(), getEncodedProcessLinkName(process)).toUri();

                fileService.createSymLink(source, destination, onlyRead, currentUser);
            } else {
                Helper.setFehlerMeldung("Aborted download to home, error - there is not current user");
            }
        } catch (IOException ioe) {
            logger.error("Exception downloadToHome()", ioe);
            Helper.setFehlerMeldung("Aborted download to home, error", ioe.getMessage());
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
            logger.error(e);
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
            URI tiffWriterURI = Paths.get(new File(imagesDirectory).getPath(), "tiffwriter.conf").toUri();
            if (new File(tiffWriterURI).exists()) {
                return;
            }
            TiffHeader tif = new TiffHeader(inProcess);
            try (BufferedWriter outfile = new BufferedWriter(
                    new OutputStreamWriter(fileService.write(tiffWriterURI), StandardCharsets.UTF_8))) {
                outfile.write(tif.getTiffAlles());
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Download aborted", e);
            logger.error(e);
        }
    }
}
