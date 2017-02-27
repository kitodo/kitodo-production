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

import org.goobi.io.SafeFile;
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

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.export.download.TiffHeader;

public class WebDav implements Serializable {

    private static final long serialVersionUID = -1929234096626965538L;
    private static final Logger logger = Logger.getLogger(WebDav.class);

    /*
     * Kopieren bzw. symbolische Links für einen Prozess in das Benutzerhome
     */

    private static String DONEDIRECTORYNAME = "fertig/";
    public WebDav(){
        DONEDIRECTORYNAME =ConfigMain.getParameter("doneDirectoryName", "fertig/");
    }


    /**
     * Retrieve all folders from one directory
     * ================================================================
     */

    public List<String> UploadFromHomeAlle(String inVerzeichnis) {
        List<String> rueckgabe = new ArrayList<String>();
        Benutzer aktuellerBenutzer = Helper.getCurrentUser();
        String VerzeichnisAlle;

        try {
            VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
        } catch (Exception ioe) {
            logger.error("Exception UploadFromHomeAlle()", ioe);
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
     * Remove Folders from Directory
     * ================================================================
     */
    // TODO: Use generic types
    public void removeFromHomeAlle(List<String> inList, String inVerzeichnis) {
        String VerzeichnisAlle;
        Benutzer aktuellerBenutzer = Helper.getCurrentUser();
        try {
            VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
        } catch (Exception ioe) {
            logger.error("Exception RemoveFromHomeAlle()", ioe);
            Helper.setFehlerMeldung("Upload stoped, error", ioe.getMessage());
            return;
        }

        for (Iterator<String> it = inList.iterator(); it.hasNext();) {
            String myname = it.next();
            FilesystemHelper.deleteSymLink(VerzeichnisAlle + myname);
        }
    }

    public void UploadFromHome(Prozess myProzess) {
        Benutzer aktuellerBenutzer = Helper.getCurrentUser();
        if (aktuellerBenutzer != null) {
            UploadFromHome(aktuellerBenutzer, myProzess);
        }
    }

    public void UploadFromHome(Benutzer inBenutzer, Prozess myProzess) {
        String nach = "";

        try {
            nach = inBenutzer.getHomeDir();
        } catch (Exception ioe) {
            logger.error("Exception UploadFromHome(...)", ioe);
            Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
            return;
        }

        /* prüfen, ob Benutzer Massenupload macht */
        if (inBenutzer.isMitMassendownload()) {
            nach += myProzess.getProjekt().getTitel() + File.separator;
            SafeFile projectDirectory = new SafeFile (nach = nach.replaceAll(" ", "__"));
            if (!projectDirectory.exists() && !projectDirectory.mkdir()) {
                List<String> param = new ArrayList<String>();
                param.add(String.valueOf(nach.replaceAll(" ", "__")));
                Helper.setFehlerMeldung(Helper.getTranslation("MassDownloadProjectCreationError", param));
                logger.error("Cannot create project directory " + nach.replaceAll(" ", "__"));
                return;
            }
        }
        nach += myProzess.getTitel() + " [" + myProzess.getId() + "]";

        /* Leerzeichen maskieren */
        nach = nach.replaceAll(" ", "__");
        SafeFile benutzerHome = new SafeFile(nach);

        FilesystemHelper.deleteSymLink(benutzerHome.getAbsolutePath());
    }

    public void DownloadToHome(Prozess myProzess, int inSchrittID, boolean inNurLesen) {
        saveTiffHeader(myProzess);
        Benutzer aktuellerBenutzer = Helper.getCurrentUser();
        String von = "";
        String userHome = "";

        try {
            von = myProzess.getImagesDirectory();
            /* UserHome ermitteln */
            userHome = aktuellerBenutzer.getHomeDir();

            /*
             * bei Massendownload muss auch das Projekt- und Fertig-Verzeichnis
             * existieren
             */
            if (aktuellerBenutzer.isMitMassendownload()) {
                SafeFile projekt = new SafeFile(userHome + myProzess.getProjekt().getTitel());
                FilesystemHelper.createDirectoryForUser(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());

                projekt = new SafeFile(userHome + DONEDIRECTORYNAME);
                FilesystemHelper.createDirectoryForUser(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
            }

        } catch (Exception ioe) {
            logger.error("Exception DownloadToHome()", ioe);
            Helper.setFehlerMeldung("Aborted download to home, error", ioe.getMessage());
            return;
        }

        /*
         * abhängig davon, ob der Download als "Massendownload" in einen
         * Projektordner erfolgen soll oder nicht, das Zielverzeichnis
         * definieren
         */
        String processLinkName = myProzess.getTitel() + "__[" + myProzess.getId() + "]";
        String nach = userHome;
        if (aktuellerBenutzer.isMitMassendownload() && myProzess.getProjekt() != null) {
            nach += myProzess.getProjekt().getTitel() + File.separator;
        }
        nach += processLinkName;

        /* Leerzeichen maskieren */
        nach = nach.replaceAll(" ", "__");

        if(logger.isInfoEnabled()){
            logger.info("von: " + von);
            logger.info("nach: " + nach);
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
            logger.error("IOException DownloadToHome()", ioe);
            Helper.setFehlerMeldung("Download aborted, IOException", ioe.getMessage());
        } catch (InterruptedException e) {
            logger.error("InterruptedException DownloadToHome()", e);
            Helper.setFehlerMeldung("Download aborted, InterruptedException", e.getMessage());
            logger.error(e);
        }
    }

    private void saveTiffHeader(Prozess inProzess) {
        try {
            /* prüfen, ob Tiff-Header schon existiert */
            if (new SafeFile(inProzess.getImagesDirectory() + "tiffwriter.conf").exists()) {
                return;
            }
            TiffHeader tif = new TiffHeader(inProzess);
            try (
                BufferedWriter outfile =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inProzess.getImagesDirectory()
                        + "tiffwriter.conf"), StandardCharsets.UTF_8));
            ) {
                outfile.write(tif.getTiffAlles());
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Download aborted", e);
            logger.error(e);
        }
    }

    public int getAnzahlBaende(String inVerzeichnis) {
        try {
            Benutzer aktuellerBenutzer = Helper.getCurrentUser();
            String VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
            SafeFile benutzerHome = new SafeFile(VerzeichnisAlle);
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("]");
                }
            };
            return benutzerHome.list(filter).length;
        } catch (Exception e) {
            logger.error(e);
            return 0;
        }
    }

}
