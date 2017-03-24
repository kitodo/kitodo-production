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

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.export.download.TiffHeader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.io.SafeFile;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.services.ServiceManager;

public class WebDav implements Serializable {
    private final ServiceManager serviceManager = new ServiceManager();
    private static final long serialVersionUID = -1929234096626965538L;
    private static final Logger myLogger = Logger.getLogger(WebDav.class);

    /*
     * Kopieren bzw. symbolische Links für einen Prozess in das Benutzerhome
     */

    private static String DONEDIRECTORYNAME = "fertig/";

    public WebDav() {
        DONEDIRECTORYNAME = ConfigMain.getParameter("doneDirectoryName", "fertig/");
    }

    /**
     * Retrieve all folders from one directory.
     */

    public List<String> UploadFromHomeAlle(String inVerzeichnis) {
        List<String> rueckgabe = new ArrayList<String>();
        User aktuellerBenutzer = Helper.getCurrentUser();
        String VerzeichnisAlle;

        try {
            VerzeichnisAlle = serviceManager.getUserService().getHomeDirectory(aktuellerBenutzer) + inVerzeichnis;
        } catch (Exception ioe) {
            myLogger.error("Exception UploadFromHomeAlle()", ioe);
            Helper.setFehlerMeldung("UploadFromHomeAlle abgebrochen, Fehler", ioe.getMessage());
            return rueckgabe;
        }

        SafeFile benutzerHome = new SafeFile(VerzeichnisAlle);

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("]");
            }
        };
        String[] dateien = benutzerHome.list(filter);
        if (dateien == null) {
            return new ArrayList<String>();
        } else {
            for (String data : dateien) {
                if (data.endsWith("/") || data.endsWith("\\")) {
                    data = data.substring(0, data.length() - 1);
                }
                if (data.contains("/")) {
                    data = data.substring(data.lastIndexOf("/"));
                }
            }
            return new ArrayList<String>(Arrays.asList(dateien));
        }

    }

    /**
     * Remove Folders from Directory.
     */
    // TODO: Use generic types
    public void removeFromHomeAlle(List<String> inList, String inVerzeichnis) {
        String VerzeichnisAlle;
        User aktuellerBenutzer = Helper.getCurrentUser();
        try {
            VerzeichnisAlle = serviceManager.getUserService().getHomeDirectory(aktuellerBenutzer) + inVerzeichnis;
        } catch (Exception ioe) {
            myLogger.error("Exception RemoveFromHomeAlle()", ioe);
            Helper.setFehlerMeldung("Upload stoped, error", ioe.getMessage());
            return;
        }

        for (Iterator<String> it = inList.iterator(); it.hasNext();) {
            String myname = it.next();
            FilesystemHelper.deleteSymLink(VerzeichnisAlle + myname);
        }
    }

    /**
     * Upload from home.
     *
     * @param myProcess
     *            Process object
     */
    public void UploadFromHome(Process myProcess) {
        User aktuellerBenutzer = Helper.getCurrentUser();
        if (aktuellerBenutzer != null) {
            UploadFromHome(aktuellerBenutzer, myProcess);
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
    public void UploadFromHome(User inBenutzer, Process myProcess) {
        String nach = "";

        try {
            nach = serviceManager.getUserService().getHomeDirectory(inBenutzer);
        } catch (Exception ioe) {
            myLogger.error("Exception UploadFromHome(...)", ioe);
            Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
            return;
        }

        /* prüfen, ob Benutzer Massenupload macht */
        if (inBenutzer.isWithMassDownload()) {
            nach += myProcess.getProject().getTitle() + File.separator;
            SafeFile projectDirectory = new SafeFile(nach = nach.replaceAll(" ", "__"));
            if (!projectDirectory.exists() && !projectDirectory.mkdir()) {
                List<String> param = new ArrayList<String>();
                param.add(String.valueOf(nach.replaceAll(" ", "__")));
                Helper.setFehlerMeldung(Helper.getTranslation("MassDownloadProjectCreationError", param));
                myLogger.error("Can not create project directory " + nach.replaceAll(" ", "__"));
                return;
            }
        }
        nach += myProcess.getTitle() + " [" + myProcess.getId() + "]";

        /* Leerzeichen maskieren */
        nach = nach.replaceAll(" ", "__");
        SafeFile benutzerHome = new SafeFile(nach);

        FilesystemHelper.deleteSymLink(benutzerHome.getAbsolutePath());
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
    public void DownloadToHome(Process myProcess, int inSchrittID, boolean inNurLesen) {
        saveTiffHeader(myProcess);
        User aktuellerBenutzer = Helper.getCurrentUser();
        String von = "";
        String userHome = "";

        try {
            von = serviceManager.getProcessService().getImagesDirectory(myProcess);
            /* UserHome ermitteln */
            userHome = serviceManager.getUserService().getHomeDirectory(aktuellerBenutzer);

            /*
             * bei Massendownload muss auch das Projekt- und Fertig-Verzeichnis
             * existieren
             */
            if (aktuellerBenutzer.isWithMassDownload()) {
                SafeFile projekt = new SafeFile(userHome + myProcess.getProject().getTitle());
                FilesystemHelper.createDirectoryForUser(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());

                projekt = new SafeFile(userHome + DONEDIRECTORYNAME);
                FilesystemHelper.createDirectoryForUser(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
            }

        } catch (Exception ioe) {
            myLogger.error("Exception DownloadToHome()", ioe);
            Helper.setFehlerMeldung("Aborted download to home, error", ioe.getMessage());
            return;
        }

        /*
         * abhängig davon, ob der Download als "Massendownload" in einen
         * Projektordner erfolgen soll oder nicht, das Zielverzeichnis
         * definieren
         */
        String processLinkName = myProcess.getTitle() + "__[" + myProcess.getId() + "]";
        String nach = userHome;
        if (aktuellerBenutzer.isWithMassDownload() && myProcess.getProject() != null) {
            nach += myProcess.getProject().getTitle() + File.separator;
        }
        nach += processLinkName;

        /* Leerzeichen maskieren */
        nach = nach.replaceAll(" ", "__");

        if (myLogger.isInfoEnabled()) {
            myLogger.info("von: " + von);
            myLogger.info("nach: " + nach);
        }

        SafeFile imagePfad = new SafeFile(von);
        SafeFile benutzerHome = new SafeFile(nach);

        // wenn der Ziellink schon existiert, dann abbrechen
        if (benutzerHome.exists()) {
            return;
        }

        String command = ConfigMain.getParameter("script_createSymLink") + " ";
        command += imagePfad + " " + benutzerHome + " ";
        if (inNurLesen) {
            command += ConfigMain.getParameter("UserForImageReading", "root");
        } else {
            command += aktuellerBenutzer.getLogin();
        }
        try {
            ShellScript.legacyCallShell2(command);
        } catch (java.io.IOException ioe) {
            myLogger.error("IOException DownloadToHome()", ioe);
            Helper.setFehlerMeldung("Download aborted, IOException", ioe.getMessage());
        } catch (InterruptedException e) {
            myLogger.error("InterruptedException DownloadToHome()", e);
            Helper.setFehlerMeldung("Download aborted, InterruptedException", e.getMessage());
            myLogger.error(e);
        }
    }

    private void saveTiffHeader(Process inProcess) {
        try {
            /* prüfen, ob Tiff-Header schon existiert */
            if (new SafeFile(serviceManager.getProcessService().getImagesDirectory(inProcess) + "tiffwriter.conf")
                    .exists()) {
                return;
            }
            TiffHeader tif = new TiffHeader(inProcess);
            try (BufferedWriter outfile = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(
                            serviceManager.getProcessService().getImagesDirectory(inProcess) + "tiffwriter.conf"),
                    StandardCharsets.UTF_8));) {
                outfile.write(tif.getTiffAlles());
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Download aborted", e);
            myLogger.error(e);
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
            String VerzeichnisAlle = serviceManager.getUserService().getHomeDirectory(aktuellerBenutzer)
                    + inVerzeichnis;
            SafeFile benutzerHome = new SafeFile(VerzeichnisAlle);
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("]");
                }
            };
            return benutzerHome.list(filter).length;
        } catch (Exception e) {
            myLogger.error(e);
            return 0;
        }
    }

}
