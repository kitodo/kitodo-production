/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

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
   
   /**
    * Diese Methode wird benötigt um die mit der Session ID verbundene Prozess ID zu erhalten. 
    * Die Implementierung dieser Methode ist optional.
    * @param sessionID
    * @return ProzessID
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400
    * ================================================================*/
   @Override
   public String get(String sessionID) throws GoobiException {
      super.get(sessionID);
      return String.valueOf(ModuleServerForm.getProcessFromShortSession(sessionID).getId().intValue());
   }

   /**
    * Diese Methode wird benötigt um die Volltextdatei zu erhalten.
    * @param sessionId
    * @return Pfad zur XML Datei (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400, 1401
    * ================================================================*/
   @Override
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
    * @param sessionId
    * @return Arbeitsverzeichnis (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400, 1401
    * ================================================================*/
   @Override
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
    * @param sessionId
    * @return Pfad zur XML Datei (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400, 1401
    * ================================================================*/
   @Override
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
    * @param sessionId
    * @return Parameter Struktur
    * @throws GoobiException: 1, 2, 4, 5, 6, 254
    * ================================================================*/
   @Override
   public HashMap<String, String> getParams(String sessionId) throws GoobiException {
      super.getParams(sessionId);
      HashMap<String, String> myMap = new HashMap<String, String>();
      Prozess p = ModuleServerForm.getProcessFromShortSession(sessionId);
      myMap.put("ruleset", ConfigMain.getParameter("RegelsaetzeVerzeichnis") + p.getRegelsatz().getDatei());
      try {
		myMap.put("tifdirectory", p.getImagesTifDirectory(false));
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
    * @param sessionId
    * @return Projekttitel (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400
    * ================================================================*/
   @Override
   public String getProject(String sessionId) throws GoobiException {
      super.getProject(sessionId);
      return ModuleServerForm.getProcessFromShortSession(sessionId).getProjekt().getTitel();
   }

   /**
    * Diese Methode liefert den Titel eines Prozesses. 
    * Die Implementierung dieser Methode ist optional.
    * @param sessionId
    * @return Prozesstitel (String)
    * @throws GoobiException: 1, 2, 4, 5, 6, 254, 1400
    * ================================================================*/
   @Override
   public String getTitle(String sessionId) throws GoobiException {
      super.getTitle(sessionId);
      return ModuleServerForm.getProcessFromShortSession(sessionId).getTitel();
   }

}
