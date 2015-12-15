package de.sub.goobi.helper.tasks;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
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
import org.goobi.io.SafeFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.ProzessDAO;

public class ProcessSwapOutTask extends LongRunningTask {

	/**
	 * No-argument constructor. Creates an empty ProcessSwapOutTask. Must be
	 * made explicit because a constructor taking an argument is present.
	 */
	public ProcessSwapOutTask() {
	}

	/**
	 * The clone constructor creates a new instance of this object. This is
	 * necessary for Threads that have terminated in order to render to run them
	 * again possible.
	 * 
	 * @param processSwapInTask
	 *            copy master to create a clone of
	 */
	public ProcessSwapOutTask(ProcessSwapOutTask processSwapOutTask) {
		super(processSwapOutTask);
	}

	/**
	 * Returns the display name of the task to show to the user.
	 * 
	 * @see de.sub.goobi.helper.tasks.INameableTask#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return Helper.getTranslation("ProcessSwapOutTask");
	}

	@Override
   public void initialize(Prozess inProzess) {
      super.initialize(inProzess);
      setTitle("Auslagerung: " + inProzess.getTitel());
   }
   
   /**
    * Aufruf als Thread
    * ================================================================*/
   @Override
public void run() {
      setStatusProgress(5);
      Helper help = new Helper();
      String swapPath = null;
      ProzessDAO dao = new ProzessDAO();
      String processDirectory = "";

      if (ConfigMain.getBooleanParameter("useSwapping")) {
		swapPath = ConfigMain.getParameter("swapPath", "");
	} else {
         setStatusMessage("swapping not activated");
         setStatusProgress(-1);
         return;
      }
      if ((swapPath == null) || (swapPath.length() == 0)) {
         setStatusMessage("no swappingPath defined");
         setStatusProgress(-1);
         return;
      }
      SafeFile swapFile = new SafeFile(swapPath);
      if (!swapFile.exists()) {
         setStatusMessage("Swap folder does not exist or is not mounted");
         setStatusProgress(-1);
         return;
      }
      try {
         processDirectory = getProzess().getProcessDataDirectoryIgnoreSwapping();
         //TODO: Don't catch Exception (the super class)
      } catch (Exception e) {
    	  logger.warn("Exception:", e);
         setStatusMessage("Error while getting process data folder: " + e.getClass().getName() + " - "
               + e.getMessage());
         setStatusProgress(-1);
         return;
      }

      SafeFile fileIn = new SafeFile(processDirectory);
      SafeFile fileOut = new SafeFile(swapPath + getProzess().getId() + File.separator);
      if (fileOut.exists()) {
         setStatusMessage(getProzess().getTitel() + ": swappingOutTarget already exists");
         setStatusProgress(-1);
         return;
      }
      fileOut.mkdir();

      /* ---------------------
       * Xml-Datei vorbereiten
      * -------------------*/
      Document doc = new Document();
      Element root = new Element("goobiArchive");
      doc.setRootElement(root);
      Element source = new Element("source").setText(fileIn.getAbsolutePath());
      Element target = new Element("target").setText(fileOut.getAbsolutePath());
      Element title = new Element("title").setText(getProzess().getTitel());
      Element mydate = new Element("date").setText(new Date().toString());
      root.addContent(source);
      root.addContent(target);
      root.addContent(title);
      root.addContent(mydate);

      /* ---------------------
       * Verzeichnisse und Dateien kopieren und anschliessend den Ordner leeren
      * -------------------*/
      setStatusProgress(50);
      try {
        setStatusMessage("copying process folder");
        Helper.copyDirectoryWithCrc32Check(fileIn, fileOut, help.getGoobiDataDirectory().length(), root);
      } catch (IOException e) {
    	  logger.warn("IOException:", e);
         setStatusMessage("IOException in copyDirectory: " + e.getMessage());
         setStatusProgress(-1);
         return;
      }
      setStatusProgress(80);
      Helper.deleteDataInDir(new SafeFile(fileIn.getAbsolutePath()));

      /* ---------------------
       * xml-Datei schreiben
      * -------------------*/
      Format format = Format.getPrettyFormat();
      format.setEncoding("UTF-8");
      try {
         setStatusMessage("writing swapped.xml");
         XMLOutputter xmlOut = new XMLOutputter(format);
         FileOutputStream fos = new FileOutputStream(processDirectory + File.separator + "swapped.xml");
         xmlOut.output(doc, fos);
         fos.close();
         //TODO: Don't catch Exception (the super class)
      } catch (Exception e) {
    	  logger.warn("Exception:", e);
         setStatusMessage(e.getClass().getName() + " in xmlOut.output: " + e.getMessage());
         setStatusProgress(-1);
         return;
      }
      setStatusProgress(90);      
      
      /* in Prozess speichern */
      try {
         setStatusMessage("saving process");
         Prozess myProzess = dao.get(getProzess().getId());
         myProzess.setSwappedOutGui(true);
         dao.save(myProzess);
      } catch (DAOException e) {
         setStatusMessage("DAOException while saving process: " + e.getMessage());
         logger.warn("DAOException:", e);
         setStatusProgress(-1);
         return;
      }
      setStatusMessage("done");
      setStatusProgress(100);
   }

	/**
	 * Calls the clone constructor to create a not yet executed instance of this
	 * thread object. This is necessary for threads that have terminated in
	 * order to render possible to restart them.
	 * 
	 * @return a not-yet-executed replacement of this thread
	 * @see de.sub.goobi.helper.tasks.EmptyTask#replace()
	 */
	@Override
	public ProcessSwapOutTask replace() {
		return new ProcessSwapOutTask(this);
	}

}
