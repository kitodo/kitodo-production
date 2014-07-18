package de.sub.goobi.helper.tasks;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.ProzessDAO;

public class ProcessSwapInTask extends LongRunningTask {

	public ProcessSwapInTask() {
	}

	public ProcessSwapInTask(ProcessSwapInTask processSwapInTask) {
		super(processSwapInTask);
	}

	@Override
	public void initialize(Prozess inProzess) {
		super.initialize(inProzess);
		setTitle("Einlagerung: " + inProzess.getTitel());
	}

	/**
	 * Aufruf als Thread ================================================================
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		setStatusProgress(5);
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
			// TODO: Don't catch Exception (the super class)
		} catch (Exception e) {
			logger.warn("Exception:", e);
			setStatusMessage("Error while getting process data folder: " + e.getClass().getName() + " - " + e.getMessage());
			setStatusProgress(-1);
			return;
		}

		File fileIn = new File(processDirectory);
		File fileOut = new File(swapPath + getProzess().getId() + File.separator);

		if (!fileOut.exists()) {
			setStatusMessage(getProzess().getTitel() + ": swappingOutTarget does not exist");
			setStatusProgress(-1);
			return;
		}
		if (!fileIn.exists()) {
			setStatusMessage(getProzess().getTitel() + ": process data folder does not exist");
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
			setStatusMessage("Error while reading swapped.xml in process data folder: " + e.getClass().getName() + " - " + e.getMessage());
			setStatusProgress(-1);
			return;
		}

		/*
		 * --------------------- alte Checksummen in HashMap schreiben -------------------
		 */
		setStatusMessage("reading checksums");
		Element rootOld = docOld.getRootElement();

		HashMap<String, String> crcMap = new HashMap<String, String>();

		// TODO: Don't use Iterators
		for (Iterator<Element> it = rootOld.getChildren("file").iterator(); it.hasNext();) {
			Element el = it.next();
			crcMap.put(el.getAttribute("path").getValue(), el.getAttribute("crc32").getValue());
		}
		Helper.deleteDataInDir(fileIn);

		/*
		 * --------------------- Dateien kopieren und Checksummen ermitteln -------------------
		 */
		Document doc = new Document();
		Element root = new Element("goobiArchive");
		doc.setRootElement(root);

		/*
		 * --------------------- Verzeichnisse und Dateien kopieren und anschliessend den Ordner leeren -------------------
		 */
		setStatusProgress(50);
		try {
			setStatusMessage("copying process files");
			Helper.copyDirectoryWithCrc32Check(fileOut, fileIn, swapPath.length(), root);
		} catch (IOException e) {
			logger.warn("IOException:", e);
			setStatusMessage("IOException in copyDirectory: " + e.getMessage());
			setStatusProgress(-1);
			return;
		}
		setStatusProgress(80);

		/*
		 * --------------------- Checksummen vergleichen -------------------
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
		 * --------------------- prüfen, ob noch Dateien fehlen -------------------
		 */
		setStatusMessage("checking missing files");
		if (crcMap.size() > 0) {
			for (String myFile : crcMap.keySet()) {
				setLongMessage(getLongMessage() + "File " + myFile + " is missing<br/>");
			}
		}

		setStatusProgress(90);

		/* in Prozess speichern */
		Helper.deleteDir(fileOut);
		try {
			setStatusMessage("saving process");
			Prozess myProzess = dao.get(getProzess().getId());
			myProzess.setSwappedOutGui(false);
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

	@Override
	public EmptyTask clone() {
		return new ProcessSwapInTask(this);
	}

}
