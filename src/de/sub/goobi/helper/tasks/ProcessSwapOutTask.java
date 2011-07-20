package de.sub.goobi.helper.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;

public class ProcessSwapOutTask extends LongRunningTask {

   @Override
   public void initialize(Prozess inProzess) {
      super.initialize(inProzess);
      setTitle("Auslagerung: " + inProzess.getTitel());
   }
   
   /**
    * Aufruf als Thread
    * ================================================================*/
   public void run() {
      setStatusProgress(5);
      Helper help = new Helper();
      String swapPath = null;
      ProzessDAO dao = new ProzessDAO();
      String processDirectory = "";

      if (ConfigMain.getBooleanParameter("useSwapping"))
         swapPath = ConfigMain.getParameter("swapPath", "");
      else {
         setStatusMessage("swapping not activated");
         setStatusProgress(-1);
         return;
      }
      if (swapPath == null || swapPath.length() == 0) {
         setStatusMessage("no swappingPath defined");
         setStatusProgress(-1);
         return;
      }
      File swapFile = new File(swapPath);
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

      File fileIn = new File(processDirectory);
      File fileOut = new File(swapPath + getProzess().getId() + File.separator);
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
        help.copyDirectoryWithCrc32Check(fileIn, fileOut, help.getGoobiDataDirectory().length(), root);
      } catch (IOException e) {
    	  logger.warn("IOException:", e);
         setStatusMessage("IOException in copyDirectory: " + e.getMessage());
         setStatusProgress(-1);
         return;
      }
      setStatusProgress(80);
      /* delete all in ProcessDataDirectory */
      help.deleteInDir(new File(fileIn.getAbsolutePath()));

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
      //Helper.getHibernateSession().close();
   }

}
