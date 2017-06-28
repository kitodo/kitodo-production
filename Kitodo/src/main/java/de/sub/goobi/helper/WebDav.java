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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("]");
            }
        };
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
     * @param inBenutzer
     *            User object
     * @param myProcess
     *            Process object
     */
    public void uploadFromHome(User inBenutzer, Process myProcess) {
        URI nach = null;

        try {
            nach = serviceManager.getUserService().getHomeDirectory(inBenutzer);
        } catch (Exception ioe) {
            logger.error("Exception uploadFromHome(...)", ioe);
            Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
            return;
        }

        /* prüfen, ob Benutzer Massenupload macht */
        if (inBenutzer.isWithMassDownload()) {
            nach = nach.resolve(myProcess.getProject().getTitle() + File.separator);
            nach = URI.create(nach.toString().replaceAll(" ", "__"));
            URI projectDirectory = nach;
            if (!fileService.fileExist(projectDirectory)
                    && !fileService.isDirectory(fileService.createResource(projectDirectory.toString()))) {
                List<String> param = new ArrayList<>();
                param.add(nach.toString().replaceAll(" ", "__"));
                Helper.setFehlerMeldung(Helper.getTranslation("MassDownloadProjectCreationError", param));
                logger.error("Can not create project directory " + URI.create(nach.toString().replaceAll(" ", "__")));
                return;
            }
        }
        nach = nach.resolve(myProcess.getTitle() + " [" + myProcess.getId() + "]");

        /* Leerzeichen maskieren */
        nach = URI.create(nach.toString().replaceAll(" ", "__"));
        URI benutzerHome = nach;

        fileService.deleteSymLink((benutzerHome));
    }

    /**
     * Download to home.
     *
     * @param myProcess
     *            Process object
     * @param inSchrittID
     *            int
     * @param inNurLesen
     *            boolean
     */
    public void downloadToHome(Process myProcess, int inSchrittID, boolean inNurLesen) {
        saveTiffHeader(myProcess);
        User aktuellerBenutzer = Helper.getCurrentUser();
        URI von;
        URI userHome;

        try {
            von = serviceManager.getFileService().getImagesDirectory(myProcess);
            /* UserHome ermitteln */
            userHome = serviceManager.getUserService().getHomeDirectory(aktuellerBenutzer);

            /*
             * bei Massendownload muss auch das Projekt- und Fertig-Verzeichnis
             * existieren
             */
            if (aktuellerBenutzer.isWithMassDownload()) {
                URI projekt = new File(userHome + myProcess.getProject().getTitle()).toURI();
                fileService.createDirectoryForUser(projekt, aktuellerBenutzer.getLogin());

                projekt = new File(userHome + DONEDIRECTORYNAME).toURI();
                fileService.createDirectoryForUser(projekt, aktuellerBenutzer.getLogin());
            }

        } catch (Exception ioe) {
            logger.error("Exception downloadToHome()", ioe);
            Helper.setFehlerMeldung("Aborted download to home, error", ioe.getMessage());
            return;
        }

        /*
         * abhängig davon, ob der Download als "Massendownload" in einen
         * Projektordner erfolgen soll oder nicht, das Zielverzeichnis
         * definieren
         */
        String processLinkName = myProcess.getTitle() + "__[" + myProcess.getId() + "]";
        URI nach = userHome;
        if (aktuellerBenutzer.isWithMassDownload() && myProcess.getProject() != null) {
            nach = nach.resolve(myProcess.getProject().getTitle() + File.separator);
        }
        nach = nach.resolve(processLinkName);

        /* Leerzeichen maskieren */
        nach = URI.create(nach.toString().replaceAll(" ", "__"));

        if (logger.isInfoEnabled()) {
            logger.info("von: " + von);
            logger.info("nach: " + nach);
        }

        File imagePfad = new File(von);
        File benutzerHome = new File(nach);

        // wenn der Ziellink schon existiert, dann abbrechen
        if (benutzerHome.exists()) {
            return;
        }

        String command = ConfigCore.getParameter("script_createSymLink") + " ";
        command += imagePfad + " " + benutzerHome + " ";
        if (inNurLesen) {
            command += ConfigCore.getParameter("UserForImageReading", "root");
        } else {
            command += aktuellerBenutzer.getLogin();
        }
        try {
            ShellScript.legacyCallShell2(command);
        } catch (IOException ioe) {
            logger.error("IOException downloadToHome()", ioe);
            Helper.setFehlerMeldung("Download aborted, IOException", ioe.getMessage());
        }
    }

    private void saveTiffHeader(Process inProcess) {
        try {
            /* prüfen, ob Tiff-Header schon existiert */
            if (new File(serviceManager.getFileService().getImagesDirectory(inProcess) + "tiffwriter.conf").exists()) {
                return;
            }
            TiffHeader tif = new TiffHeader(inProcess);
            try (BufferedWriter outfile = new BufferedWriter(new OutputStreamWriter(
                    fileService.write(
                            serviceManager.getFileService().getImagesDirectory(inProcess).resolve("tiffwriter.conf")),
                    StandardCharsets.UTF_8));) {
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
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("]");
                }
            };
            return fileService.getSubUris(filter, verzeichnisAlle).size();
        } catch (Exception e) {
            logger.error(e);
            return 0;
        }
    }

}
