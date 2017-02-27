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

package de.sub.goobi.helper.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.goobi.io.SafeFile;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.helper.CopyFile;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.ProzessDAO;

public class ProcessSwapOutTask extends LongRunningTask {

    private static final Logger logger = Logger.getLogger(ProcessSwapOutTask.class);

    /**
     * Copies all files under srcDir to dstDir. If dstDir does not exist, it will be created.
     */

    static void copyDirectoryWithCrc32Check(SafeFile srcDir, SafeFile dstDir, int goobipathlength, Element inRoot)
            throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
                dstDir.setLastModified(srcDir.lastModified());
            }
            String[] children = srcDir.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectoryWithCrc32Check(new SafeFile(srcDir, children[i]), new SafeFile(dstDir, children[i]),
                        goobipathlength, inRoot);
            }
        } else {
            Long crc = CopyFile.start(srcDir, dstDir);
            Element file = new Element("file");
            file.setAttribute("path", srcDir.getAbsolutePath().substring(goobipathlength));
            file.setAttribute("crc32", String.valueOf(crc));
            inRoot.addContent(file);
        }
    }

    /**
     * Deletes all files and subdirectories under dir. But not the dir itself and no metadata files.
     */
    static boolean deleteDataInDir(SafeFile dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                if (!children[i].endsWith(".xml")) {
                    boolean success = new SafeFile(dir, children[i]).deleteDir();
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

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
     * @param processSwapOutTask
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
        copyDirectoryWithCrc32Check(fileIn, fileOut, help.getGoobiDataDirectory().length(), root);
      } catch (IOException e) {
          logger.warn("IOException:", e);
         setStatusMessage("IOException in copyDirectory: " + e.getMessage());
         setStatusProgress(-1);
         return;
      }
      setStatusProgress(80);
      deleteDataInDir(new SafeFile(fileIn.getAbsolutePath()));

      /* ---------------------
       * xml-Datei schreiben
      * -------------------*/
      Format format = Format.getPrettyFormat();
      format.setEncoding("UTF-8");
      try (FileOutputStream fos = new FileOutputStream(processDirectory + File.separator + "swapped.xml")) {
         setStatusMessage("writing swapped.xml");
         XMLOutputter xmlOut = new XMLOutputter(format);
         xmlOut.output(doc, fos);
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
