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

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.goobi.io.SafeFile;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

public class ProcessSwapInTask extends LongRunningTask {
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * No-argument constructor. Creates an empty ProcessSwapInTask. Must be made
     * explicit because a constructor taking an argument is present.
     */
    public ProcessSwapInTask() {
    }

    /**
     * The clone constructor creates a new instance of this object. This is
     * necessary for Threads that have terminated in order to render to run them
     * again possible.
     *
     * @param processSwapInTask
     *            copy master to create a clone of
     */
    public ProcessSwapInTask(ProcessSwapInTask processSwapInTask) {
        super(processSwapInTask);
    }

    @Override
    public void initialize(Process inputProcess) {
        super.initialize(inputProcess);
        setTitle("Einlagerung: " + inputProcess.getTitle());
    }

    /**
     * Returns the display name of the task to show to the user.
     *
     * @see de.sub.goobi.helper.tasks.INameableTask#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return Helper.getTranslation("ProcessSwapInTask");
    }

    /**
     * Aufruf als Thread.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        setStatusProgress(5);
        String swapPath = null;
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
            processDirectory = serviceManager.getProcessService().getProcessDataDirectoryIgnoreSwapping(getProcess());
            // TODO: Don't catch Exception (the super class)
        } catch (Exception e) {
            logger.warn("Exception:", e);
            setStatusMessage(
                    "Error while getting process data folder: " + e.getClass().getName() + " - " + e.getMessage());
            setStatusProgress(-1);
            return;
        }

        SafeFile fileIn = new SafeFile(processDirectory);
        SafeFile fileOut = new SafeFile(swapPath + getProcess().getId() + File.separator);

        if (!fileOut.exists()) {
            setStatusMessage(getProcess().getTitle() + ": swappingOutTarget does not exist");
            setStatusProgress(-1);
            return;
        }
        if (!fileIn.exists()) {
            setStatusMessage(getProcess().getTitle() + ": process data folder does not exist");
            setStatusProgress(-1);
            return;
        }

        SAXBuilder builder = new SAXBuilder();
        Document docOld;
        try {
            File swapLogFile = new File(processDirectory, "swapped.xml");
            docOld = builder.build(swapLogFile);
            // TODO: Don't catch Exception (the super class)
        } catch (Exception e) {
            logger.warn("Exception:", e);
            setStatusMessage("Error while reading swapped.xml in process data folder: " + e.getClass().getName() + " - "
                    + e.getMessage());
            setStatusProgress(-1);
            return;
        }

        /*
         * alte Checksummen in HashMap schreiben
         */
        setStatusMessage("reading checksums");
        Element rootOld = docOld.getRootElement();

        HashMap<String, String> crcMap = new HashMap<String, String>();

        // TODO: Don't use Iterators
        for (Iterator<Element> it = rootOld.getChildren("file").iterator(); it.hasNext();) {
            Element el = it.next();
            crcMap.put(el.getAttribute("path").getValue(), el.getAttribute("crc32").getValue());
        }
        ProcessSwapOutTask.deleteDataInDir(fileIn);

        /*
         * Dateien kopieren und Checksummen ermitteln
         */
        Document doc = new Document();
        Element root = new Element("goobiArchive");
        doc.setRootElement(root);

        /*
         * Verzeichnisse und Dateien kopieren und anschliessend den Ordner
         * leeren
         */
        setStatusProgress(50);
        try {
            setStatusMessage("copying process files");
            ProcessSwapOutTask.copyDirectoryWithCrc32Check(fileOut, fileIn, swapPath.length(), root);
        } catch (IOException e) {
            logger.warn("IOException:", e);
            setStatusMessage("IOException in copyDirectory: " + e.getMessage());
            setStatusProgress(-1);
            return;
        }
        setStatusProgress(80);

        /*
         * Checksummen vergleichen
         */
        setStatusMessage("checking checksums");
        // TODO: Don't use Iterators
        for (Iterator<Element> it = root.getChildren("file").iterator(); it.hasNext();) {
            Element el = it.next();
            String newPath = el.getAttribute("path").getValue();
            String newCrc = el.getAttribute("crc32").getValue();
            if (crcMap.containsKey(newPath)) {
                if (!crcMap.get(newPath).equals(newCrc)) {
                    setLongMessage(getLongMessage() + "File " + newPath + " has different checksum<br/>");
                }
                crcMap.remove(newPath);
            }
        }

        setStatusProgress(85);
        /*
         * prüfen, ob noch Dateien fehlen
         */
        setStatusMessage("checking missing files");
        if (crcMap.size() > 0) {
            for (String myFile : crcMap.keySet()) {
                setLongMessage(getLongMessage() + "File " + myFile + " is missing<br/>");
            }
        }

        setStatusProgress(90);

        /* in Prozess speichern */
        fileOut.deleteDir();
        try {
            setStatusMessage("saving process");
            Process myProcess = serviceManager.getProcessService().find(getProcess().getId());
            myProcess.setSwappedOutGui(false);
            serviceManager.getProcessService().save(myProcess);
        } catch (DAOException e) {
            setStatusMessage("DAOException while saving process: " + e.getMessage());
            logger.warn("DAOException:", e);
            setStatusProgress(-1);
            return;
        } catch (IOException e) {
            logger.warn("IOException:", e);
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
    public ProcessSwapInTask replace() {
        return new ProcessSwapInTask(this);
    }

}
