package de.sub.goobi.helper;
//TODO: Replace with a VFS
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Export.download.TiffHeader;
import de.sub.goobi.config.ConfigMain;

public class WebDav {
   private static final Logger myLogger = Logger.getLogger(WebDav.class);


   /*#####################################################
    #####################################################
    ##																															 
    ##		Kopieren bzw. symbolische Links für einen Prozess in das Benutzerhome									
    ##                                                   															    
    #####################################################
    ####################################################*/

   /**
    * Retrieve all folders from one directory
   * ================================================================*/

   public List<String> UploadFromHomeAlle(String inVerzeichnis) {
      List<String> rueckgabe = new ArrayList<String>();
      Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
      String VerzeichnisAlle;

      try {
         VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
         //         new Helper().setTomcatBenutzerrechte(VerzeichnisAlle);
      } catch (Exception ioe) {
         myLogger.error("Exception UploadFromHomeAlle()", ioe);
         new Helper().setFehlerMeldung("UploadFromHomeAlle abgebrochen, Fehler", ioe.getMessage());
         return rueckgabe;
      }

      //      myLogger.debug("Upload-Verzeichnis: " + VerzeichnisAlle);
      File benutzerHome = new File(VerzeichnisAlle);

      FilenameFilter filter = new FilenameFilter() {
         public boolean accept(File dir, String name) {
            return name.endsWith("]");
         }
      };
      String[] dateien = benutzerHome.list(filter);
      if (dateien == null) {
         return new ArrayList<String>();
      } else {
         return new ArrayList<String>(Arrays.asList(dateien));
      }

   }

   /**
    * Remove Folders from Directory
   * ================================================================*/
   //TODO: Use generic types
   public void removeFromHomeAlle(List inList, String inVerzeichnis) {
      String VerzeichnisAlle;
      Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
      try {
         VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
      } catch (Exception ioe) {
         myLogger.error("Exception RemoveFromHomeAlle()", ioe);
         new Helper().setFehlerMeldung("Upload abgebrochen, Fehler", ioe.getMessage());
         return;
      }

      for (Iterator it = inList.iterator(); it.hasNext();) {
         String myname = (String) it.next();
         String command = ConfigMain.getParameter("script_deleteSymLink") + " ";
         command += VerzeichnisAlle + myname;
         //         myLogger.debug(command);
         try {
            Runtime.getRuntime().exec(command);
         } catch (java.io.IOException ioe) {
            myLogger.error("IOException UploadFromHomeAlle()", ioe);
            new Helper().setFehlerMeldung("UploadFromHomeAlle abgebrochen, Fehler", ioe.getMessage());
            return;
         }
      }
   }

   

   public void UploadFromHome(Prozess myProzess) {
      Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
      UploadFromHome(aktuellerBenutzer, myProzess);
   }

   

   public void UploadFromHome(Benutzer inBenutzer, Prozess myProzess) {
      String nach = "";

      try {
         nach = inBenutzer.getHomeDir();
      } catch (Exception ioe) {
         myLogger.error("Exception UploadFromHome(...)", ioe);
         new Helper().setFehlerMeldung("UploadFromHome abgebrochen, Fehler", ioe.getMessage());
         return;
      }

      /* prüfen, ob Benutzer Massenupload macht */
      if (inBenutzer != null && inBenutzer.isMitMassendownload())
         nach += myProzess.getProjekt().getTitel() + File.separator;
      nach += myProzess.getTitel() + " [" + myProzess.getId() + "]";

      /* Leerzeichen maskieren */
      nach = nach.replaceAll(" ", "__");
      File benutzerHome = new File(nach);

      String command = ConfigMain.getParameter("script_deleteSymLink") + " ";
      command += benutzerHome;
      //      myLogger.debug(command);

      try {
    	  //TODO: Use ProcessBuilder
         Runtime.getRuntime().exec(command);
      } catch (java.io.IOException ioe) {
         myLogger.error("IOException UploadFromHome", ioe);
         new Helper().setFehlerMeldung("UploadFromHome abgebrochen, Fehler", ioe.getMessage());
      }
   }

   

   public void DownloadToHome(Prozess myProzess, int inSchrittID, boolean inNurLesen) {
      Helper help = new Helper();
      saveTiffHeader(myProzess);
      Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
      String von = "";
      String userHome = "";

      try {
         von = myProzess.getImagesDirectory();
         /* UserHome ermitteln */
         userHome = aktuellerBenutzer.getHomeDir();

         /* bei Massendownload muss auch das Projekt- und Fertig-Verzeichnis existieren */
         if (aktuellerBenutzer.isMitMassendownload()) {
            File projekt = new File(userHome + myProzess.getProjekt().getTitel());
            if (!projekt.exists())
               help.createUserDirectory(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
            projekt = new File(userHome + "fertig" + File.separator);
            if (!projekt.exists())
               help.createUserDirectory(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
         }

      } catch (Exception ioe) {
         myLogger.error("Exception DownloadToHome()", ioe);
         new Helper().setFehlerMeldung("DownloadToHome abgebrochen, Fehler", ioe.getMessage());
         return;
      }

      /* abhängig davon, ob der Download als "Massendownload" in einen Projektordner erfolgen soll oder nicht, 
       * das Zielverzeichnis definieren*/
      String processLinkName = myProzess.getTitel() + "__[" + myProzess.getId() + "]";
      String nach = userHome;
      if (aktuellerBenutzer.isMitMassendownload() && myProzess.getProjekt() != null)
         nach += myProzess.getProjekt().getTitel() + File.separator;
      nach += processLinkName;

      /* Leerzeichen maskieren */
      nach = nach.replaceAll(" ", "__");

      myLogger.info("von: " + von);
      myLogger.info("nach: " + nach);

      File imagePfad = new File(von);
      File benutzerHome = new File(nach);

     
      // wenn der Ziellink schon existiert, dann abbrechen
      if (benutzerHome.exists())
         return;

      String command = ConfigMain.getParameter("script_createSymLink") + " ";
      command += imagePfad + " " + benutzerHome + " ";
      if (inNurLesen)
         command += ConfigMain.getParameter("UserForImageReading", "root");
      else
         command += aktuellerBenutzer.getLogin();
      try {
         //         Runtime.getRuntime().exec(command);

         help.callShell2(command);
         new Helper().setMeldung("Verzeichnis in Benutzerhome angelegt: ", processLinkName);
      } catch (java.io.IOException ioe) {
         myLogger.error("IOException DownloadToHome()", ioe);
         help.setFehlerMeldung("Download abgebrochen, IOException", ioe.getMessage());
      } catch (InterruptedException e) {
         myLogger.error("InterruptedException DownloadToHome()", e);
         help.setFehlerMeldung("Download abgebrochen, InterruptedException", e.getMessage());
         e.printStackTrace();
      }
   }

   

   private void saveTiffHeader(Prozess inProzess) {
      try {
         /* prüfen, ob Tiff-Header schon existiert */
         if (new File(inProzess.getImagesDirectory() + "tiffwriter.conf").exists())
            return;
         TiffHeader tif = new TiffHeader(inProzess);
         BufferedWriter outfile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inProzess
               .getImagesDirectory()
               + "tiffwriter.conf"), "8859_1"));
         outfile.write(tif.getTiffAlles());
         outfile.close();
      } catch (Exception e) {
         new Helper().setFehlerMeldung("Download abgebrochen", e);
         e.printStackTrace();
      }
   }

   

   public int getAnzahlBaende(String inVerzeichnis) {
      try {
         Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
         String VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
         File benutzerHome = new File(VerzeichnisAlle);
         FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
               return name.endsWith("]");
            }
         };
         return benutzerHome.list(filter).length;
      } catch (Exception e) {
         e.printStackTrace();
         return 0;
      }
   }

   

   //TODO: Remove this Methods - Use FileUtils, as log as it's still there ;-)
   /*
   public int getAnzahlImages(String inVerzeichnis) {
      try {
         return getAnzahlImages2(new File(inVerzeichnis));
      } catch (Exception e) {
         e.printStackTrace();
         return 0;
      }
   }

   // Process all files and directories under dir
   private int getAnzahlImages2(File inDir) {
      int anzahl = 0;
      if (inDir.isDirectory()) {
         // die Images zählen
     
         FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
               return name.endsWith(".tif");
            }
         };
         anzahl = inDir.list(filter).length;

         //die Unterverzeichnisse durchlaufen
         String[] children = inDir.list();
         for (int i = 0; i < children.length; i++) {
            anzahl += getAnzahlImages2(new File(inDir, children[i]));
         }
      }
      return anzahl;
   }
   */
}
