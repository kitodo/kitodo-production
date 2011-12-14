package de.sub.goobi.modul;

import java.io.IOException;
import java.util.HashMap;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.forms.ModuleServerForm;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.goobi.module.api.dataprovider.process.ProcessImpl;
import de.unigoettingen.goobi.module.api.exception.GoobiException;

/**
 * Das ist die Implementierung von ProcessInterface. 
 * Wird auf Goobi-Seiten Ausgeführt
 * Ist auch vorläufer für GoobiEngine
 * 
 * Erweitert um die individuellen Api-Aufrufe
 * 
 * @author Igor Toker
 */
public class ExtendedProzessImpl extends ProcessImpl {
//   Helper help = new Helper();
   
   /**
    * Diese Methode wird benötigt um die mit der Session ID verbundene Prozess ID zu erhalten. 
    * Die Implementierung dieser Methode ist optional.
    * @param SessionID
    * @return ProzessID
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400
    * ================================================================*/
   public String get(String sessionID) throws GoobiException {
      super.get(sessionID);
      return String.valueOf(ModuleServerForm.getProcessFromShortSession(sessionID).getId().intValue());
   }

   /**
    * Diese Methode wird benötigt um die Volltextdatei zu erhalten.
    * @param SessionID
    * @return Pfad zur XML Datei (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400, 1401
    * ================================================================*/
   public String getFullTextFile(String sessionId) throws GoobiException {
      super.getFullTextFile(sessionId);
      try {
         return ModuleServerForm.getProcessFromShortSession(sessionId).getFulltextFilePath();
      } catch (IOException e) {
         throw new GoobiException(1300, "******** wrapped IOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      } catch (InterruptedException e) {
         throw new GoobiException(1300, "******** wrapped InterruptedException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      } catch (SwapException e) {
         throw new GoobiException(1300, "******** wrapped SwapException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      } catch (DAOException e) {
         throw new GoobiException(1300, "******** wrapped DAOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      }
   }

   /**
    * Diese Methode wird benötigt um das relative Arbeisverzeichnis zu erhalten.
    * @param SessionID
    * @return Arbeitsverzeichnis (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400, 1401
    * ================================================================*/
   public String getImageDir(String sessionId) throws GoobiException {
      super.getImageDir(sessionId);
      try {
         return ModuleServerForm.getProcessFromShortSession(sessionId).getImagesDirectory();
      } catch (IOException e) {
         throw new GoobiException(1300, "******** wrapped IOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      } catch (InterruptedException e) {
         throw new GoobiException(1300, "******** wrapped InterruptedException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      } catch (SwapException e) {
         throw new GoobiException(1300, "******** wrapped SwapException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      } catch (DAOException e) {
         throw new GoobiException(1300, "******** wrapped DAOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      }
   }

   /**
    * Diese Methode wird benötigt um die Metadatendatei zu erhalten.
    * @param SessionID
    * @return Pfad zur XML Datei (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400, 1401
    * ================================================================*/
   public String getMetadataFile(String sessionId) throws GoobiException {
      super.getMetadataFile(sessionId);
      try {
         return ModuleServerForm.getProcessFromShortSession(sessionId).getMetadataFilePath();
      } catch (IOException e) {
         throw new GoobiException(1300, "******** wrapped IOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      } catch (InterruptedException e) {
         throw new GoobiException(1300, "******** wrapped InterruptedException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      } catch (SwapException e) {
         throw new GoobiException(1300, "******** wrapped SwapException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      } catch (DAOException e) {
         throw new GoobiException(1300, "******** wrapped DAOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      }
   }

   /**
    * Diese Methode wird benutzt um die Parameter für den Aufruf des Moduls zu bekommen. 
    * Die Implementierung dieser Methode ist optional.
    * @param SessionID
    * @return Parameter Struktur
    * @throws GoobiException: 1, 2, 4, 5, 6, 254
    * ================================================================*/
   public HashMap<String, String> getParams(String sessionId) throws GoobiException {
      super.getParams(sessionId);
      HashMap<String, String> myMap = new HashMap<String, String>();
      Prozess p = ModuleServerForm.getProcessFromShortSession(sessionId);
      myMap.put("ruleset", ConfigMain.getParameter("RegelsaetzeVerzeichnis") + p.getRegelsatz().getDatei());
      try {
		myMap.put("tifdirectory", p.getImagesTifDirectory());
		} catch (IOException e) {
	        throw new GoobiException(1300, "******** wrapped IOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
		} catch (InterruptedException e) {
	        throw new GoobiException(1300, "******** wrapped InterruptedException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
		} catch (SwapException e) {
	        throw new GoobiException(1300, "******** wrapped SwapException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
		} catch (DAOException e) {
	        throw new GoobiException(1300, "******** wrapped DAOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));	
	    }
	    return myMap;
   }

   /**
    * Diese Methode liefert das Projekt eines Prozesses. 
    * Die Implementierung dieser Methode ist optional.
    * @param SessionID
    * @return Projekttitel (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400
    * ================================================================*/
   public String getProject(String sessionId) throws GoobiException {
      super.getProject(sessionId);
      return ModuleServerForm.getProcessFromShortSession(sessionId).getProjekt().getTitel();
   }

   /**
    * Diese Methode liefert den Titel eines Prozesses. 
    * Die Implementierung dieser Methode ist optional.
    * @param SessionID
    * @return Prozesstitel (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400
    * ================================================================*/
   public String getTitle(String sessionId) throws GoobiException {
      super.getTitle(sessionId);
      return ModuleServerForm.getProcessFromShortSession(sessionId).getTitel();
   }

}
