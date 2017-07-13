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
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.filters.FileNameEndsWithFilter;
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
        List<URI> rueckgabe = new ArrayList<>();
        User aktuellerBenutzer = Helper.getCurrentUser();
        URI directoryName;

        try {
            directoryName = serviceManager.getUserService().getHomeDirectory(aktuellerBenutzer).resolve(inVerzeichnis);
        } catch (Exception ioe) {
            logger.error("Exception uploadFromHomeAlle()", ioe);
            Helper.setFehlerMeldung("uploadFromHomeAlle abgebrochen, Fehler", ioe.getMessage());
            return rueckgabe;
        }

        URI benutzerHome = directoryName;
        FilenameFilter filter = new FileNameEndsWithFilter("]");
        ArrayList<URI> dateien = fileService.getSubUris(filter, benutzerHome);
        for (URI data : dateien) {
            String dataString = data.toString();
            if (dataString.endsWith("/") || dataString.endsWith("\\")) {
                data = URI.create(dataString.substring(0, dataString.length() - 1));
            }
            if (data.toString().contains("/")) {
                data = URI.create(dataString.substring(dataString.lastIndexOf("/")));
            }
        }
        return dateien;

    }

    /**
     * Remove Folders from Directory.
     */
    // TODO: Use generic types
    public void removeAllFromHome(List<URI> inList, URI inVerzeichnis) {
        URI verzeichnisAlle;
        User aktuellerBenutzer = Helper.getCurrentUser();
        try {
            verzeichnisAlle = serviceManager.getUserService().getHomeDirectory(aktuellerBenutzer)
                    .resolve(inVerzeichnis);
        } catch (Exception ioe) {
            logger.error("Exception RemoveFromHomeAlle()", ioe);
            Helper.setFehlerMeldung("Upload stoped, error", ioe.getMessage());
            return;
        }

        for (Iterator<URI> it = inList.iterator(); it.hasNext();) {
            URI myname = it.next();
            fileService.deleteSymLink(verzeichnisAlle.resolve(myname));
        }
    }

    /**
     * Upload from home.
     *
     * @param myProcess
     *            Process object
     */
    public void uploadFromHome(Process myProcess) {
        User aktuellerBenutzer = Helper.getCurrentUser();
        if (aktuellerBenutzer != null) {
            uploadFromHome(aktuellerBenutzer, myProcess);
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
        } catch (Exception ioe) {
            logger.error("Exception uploadFromHome(...)", ioe);
            Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
            return;
        }

        /* prüfen, ob Benutzer Massenupload macht */
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
        }
        destination = Paths.get(new File(destination).getPath(), getEncodedProcessLinkName(process)).toUri();
        fileService.deleteSymLink((destination));
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
            source = serviceManager.getFileService().getImagesDirectory(process);
            userHome = serviceManager.getUserService().getHomeDirectory(currentUser);

            /*
             * bei Massendownload muss auch das Projekt- und Fertig-Verzeichnis existieren
             */
            if (currentUser.isWithMassDownload()) {
                URI project = Paths.get(userHome + process.getProject().getTitle()).toUri();
                fileService.createDirectoryForUser(project, currentUser.getLogin());

                project = Paths.get(userHome + DONEDIRECTORYNAME).toUri();
                fileService.createDirectoryForUser(project, currentUser.getLogin());
            }
        } catch (Exception ioe) {
            logger.error("Exception downloadToHome()", ioe);
            Helper.setFehlerMeldung("Aborted download to home, error", ioe.getMessage());
            return;
        }

        URI destination = userHome;
        if (currentUser.isWithMassDownload() && process.getProject() != null) {
            destination = Paths.get(new File(destination).getPath(), process.getProject().getTitle()).toUri();
        }
        destination = Paths.get(new File(destination).getPath(), getEncodedProcessLinkName(process)).toUri();

        if (logger.isInfoEnabled()) {
            logger.info("source: " + source);
            logger.info("destination: " + destination);
        }

        if (!fileService.createSymLink(source, destination, onlyRead, currentUser)) {
            return;
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
            try (BufferedWriter outfile = new BufferedWriter(new OutputStreamWriter(
                    fileService.write(tiffWriterURI), StandardCharsets.UTF_8))) {
                outfile.write(tif.getTiffAlles());
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Download aborted", e);
            logger.error(e);
        }
    }

    /**
     * Get amount.
     *
     * @param inVerzeichnis
     *            String
     * @return int
     */
    public int getAnzahlBaende(String inVerzeichnis) {
        try {
            User aktuellerBenutzer = Helper.getCurrentUser();
            URI verzeichnisAlle = serviceManager.getUserService().getHomeDirectory(aktuellerBenutzer)
                    .resolve(inVerzeichnis);
            FilenameFilter filter = new FileNameEndsWithFilter("]");
            return fileService.getSubUris(filter, verzeichnisAlle).size();
        } catch (Exception e) {
            logger.error(e);
            return 0;
        }
    }

}
