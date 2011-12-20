/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.metadaten;

import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManagerException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;

public class MetadatenImagesHelper {
	private static final Logger logger = Logger.getLogger(MetadatenImagesHelper.class);
	private Prefs myPrefs;
	private DigitalDocument mydocument;
	private int myLastImage = 0;

	public MetadatenImagesHelper(Prefs inPrefs, DigitalDocument inDocument) {
		myPrefs = inPrefs;
		mydocument = inDocument;
	}

	/**
	 * Markus baut eine Seitenstruktur aus den vorhandenen Images ---------------- Steps - ---------------- Validation of images compare existing
	 * number images with existing number of page DocStructs if it is the same don't do anything if DocStructs are less add new pages to
	 * physicalDocStruct if images are less delete pages from the end of pyhsicalDocStruct --------------------------------
	 * 
	 * @return null
	 * @throws TypeNotAllowedForParentException
	 * @throws TypeNotAllowedForParentException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws DAOException
	 * @throws SwapException
	 */
	public void createPagination(Prozess inProzess) throws TypeNotAllowedForParentException, IOException, InterruptedException, SwapException,
			DAOException {
		DocStruct physicaldocstruct = mydocument.getPhysicalDocStruct();

		/*-------------------------------- 
		 * der physische Baum wird nur
		 * angelegt, wenn er noch nicht existierte
		 * --------------------------------*/
		if (physicaldocstruct == null) {
			DocStructType dst = myPrefs.getDocStrctTypeByName("BoundBook");
			physicaldocstruct = mydocument.createDocStruct(dst);

			/*-------------------------------- 
			 * Probleme mit dem FilePath
			 * -------------------------------- */
			MetadataType MDTypeForPath = myPrefs.getMetadataTypeByName("pathimagefiles");
			try {
				Metadata mdForPath = new Metadata(MDTypeForPath);
				// mdForPath.setType(MDTypeForPath);
				// TODO: add the possibilty for using other image formats
				if (SystemUtils.IS_OS_WINDOWS) {
					mdForPath.setValue("file:/" + inProzess.getImagesDirectory() + inProzess.getTitel().trim() + "_tif");
				} else {
					mdForPath.setValue("file://" + inProzess.getImagesDirectory() + inProzess.getTitel().trim() + "_tif");
				}
				physicaldocstruct.addMetadata(mdForPath);
			} catch (MetadataTypeNotAllowedException e1) {
			} catch (DocStructHasNoTypeException e1) {
			}
			mydocument.setPhysicalDocStruct(physicaldocstruct);
		}

		checkIfImagesValid(inProzess, inProzess.getImagesTifDirectory());

		/*------------------------------- 
		 * retrieve existing pages/images
		 * -------------------------------*/
		DocStructType newPage = myPrefs.getDocStrctTypeByName("page");
		List<DocStruct> oldPages = physicaldocstruct.getAllChildrenByTypeAndMetadataType("page", "*");
		if (oldPages == null) {
			oldPages = new ArrayList<DocStruct>();
		}

		/*-------------------------------- 
		 * add new page/images if necessary
		 * --------------------------------*/
		if (oldPages.size() < myLastImage) {
			for (int i = oldPages.size(); i < myLastImage; i++) {
				DocStruct dsPage = mydocument.createDocStruct(newPage);
				try {

					/*
					 * -------------------------------- die physischen Seiten anlegen, sind nicht änderbar für den Benutzer
					 * --------------------------------
					 */
					physicaldocstruct.addChild(dsPage);
					MetadataType mdt = myPrefs.getMetadataTypeByName("physPageNumber");
					Metadata mdTemp = new Metadata(mdt);
					// mdTemp.setType(mdt);
					mdTemp.setValue(String.valueOf(i + 1));
					dsPage.addMetadata(mdTemp);

					/*
					 * -------------------------------- die logischen Seitennummern anlegen, die der Benutzer auch ändern kann
					 * --------------------------------
					 */
					mdt = myPrefs.getMetadataTypeByName("logicalPageNumber");
					mdTemp = new Metadata(mdt);
					// mdTemp.setType(mdt);
					mdTemp.setValue(String.valueOf(i + 1));
					dsPage.addMetadata(mdTemp);

					// myLogger.debug("fertig mit Paginierung für Nr. " + i +
					// " von " + myBildLetztes);
				} catch (TypeNotAllowedAsChildException e) {
					logger.error(e);
				} catch (MetadataTypeNotAllowedException e) {
					logger.error(e);
				}
			}
		}

		else if (oldPages.size() > myLastImage) {
			MetadataType mdt = myPrefs.getMetadataTypeByName("physPageNumber");
			for (DocStruct page : oldPages) {
				List<? extends Metadata> mdts = page.getAllMetadataByType(mdt);
				if (mdts.size() != 1) {
					throw new SwapException("found page DocStruct with more or less than 1 pysical pagination");
					// return myLastImage;
				}
				/*
				 * delete page DocStruct, if physical pagenumber higher than last imagenumber
				 */
				if (Integer.parseInt(mdts.get(0).getValue()) > myLastImage) {
					physicaldocstruct.removeChild(page);
					List<Reference> refs = new ArrayList<Reference>(page.getAllFromReferences());
					for (ugh.dl.Reference ref : refs) {
						ref.getSource().removeReferenceTo(page);
					}
				}
			}

		}
	}

	// TODO: Try to replace some functionality with ContentServer2 (via HTTP)

	/**
	 * scale given image file to png using internal embedded content server
	 * 
	 * @throws ImageManagerException
	 * @throws IOException
	 * @throws ImageManipulatorException
	 */
	public void scaleFile(String inFileName, String outFileName, int inSize, int intRotation) throws ImageManagerException, IOException,
			ImageManipulatorException {

		int tmpSize  = inSize / 3;
		if (tmpSize < 1) {
			tmpSize = 1;
		}
		if (ConfigMain.getParameter("ContentServerUrl") == null) {
			ImageManager im = new ImageManager(new File(inFileName).toURI().toURL());
			RenderedImage ri = im.scaleImageByPixel(tmpSize, tmpSize, ImageManager.SCALE_BY_PERCENT, intRotation);
			JpegInterpreter pi = new JpegInterpreter(ri);
			FileOutputStream outputFileStream = new FileOutputStream(outFileName);
			pi.writeToStream(null, outputFileStream);
			outputFileStream.close();
		} else {
			String cs = ConfigMain.getParameter("ContentServerUrl") + inFileName + "&scale=" + tmpSize + "&rotate=" + intRotation + "&format=jpg";
			cs = cs.replace("\\", "/");
			URL csUrl = new URL(cs);
			HttpClient httpclient = new HttpClient();
			GetMethod method = new GetMethod(csUrl.toString());
			Integer contentServerTimeOut = ConfigMain.getIntParameter("goobiContentServerTimeOut", 60000);
			method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);
			int statusCode = httpclient.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				return;
			}
			InputStream inStream = method.getResponseBodyAsStream();
			BufferedInputStream bis = new BufferedInputStream(inStream);
			FileOutputStream fos = new FileOutputStream(outFileName);
			byte[] bytes = new byte[8192];
			int count = bis.read(bytes);
			while (count != -1 && count <= 8192) {
				fos.write(bytes, 0, count);
				count = bis.read(bytes);
			}
			if (count != -1) {
				fos.write(bytes, 0, count);
			}
			fos.close();
			bis.close();
		}
	}

	// Add a method to validate the image files

	/**
	 * die Images eines Prozesses auf Vollständigkeit prüfen ================================================================
	 * 
	 * @throws DAOException
	 * @throws SwapException
	 */
	public boolean checkIfImagesValid(Prozess inProzess, String folder) throws IOException, InterruptedException, SwapException, DAOException {
		boolean isValid = true;
		myLastImage = 0;

		/*-------------------------------- 
		 * alle Bilder durchlaufen und dafür
		 * die Seiten anlegen 
		 * --------------------------------*/
		File dir = new File(folder);
		if (dir.exists()) {
			String[] dateien = dir.list(new Helper().getFilter());
			if (dateien == null || dateien.length == 0) {
				Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] No images found");
				return false;
			}

			// ArrayList<String> images = getImageFiles(inProzess);
			myLastImage = dateien.length;
			if (ConfigMain.getParameter("ImagePrefix", "\\d{8}").equals("\\d{8}")) {
				List<String> filesDirs = Arrays.asList(dateien);
				Collections.sort(filesDirs);
				// TODO: How about other naming conventions?
				int counter = 1;
				int myDiff = 0;
				String curFile = null;
				try {
					for (Iterator<String> iterator = filesDirs.iterator(); iterator.hasNext(); counter++) {
						curFile = (String) iterator.next();
						int curFileNumber = Integer.parseInt(curFile.substring(0, curFile.indexOf(".")));
						if (curFileNumber != counter + myDiff) {
							Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] expected Image " + (counter + myDiff) + " but found File "
									+ curFile);
							myDiff = curFileNumber - counter;
							isValid = false;
						}
					}
				} catch (NumberFormatException e1) {
					// TODO: Use a logger
					isValid = false;
					Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] Filename of image wrong - not an 8-digit-number: " + curFile);
				}
				return isValid;
			}
			return true;
		}
		Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] No image-folder found");
		return false;
	}

	private class GoobiImageFileComparator implements Comparator<String> {

		public int compare(String s1, String s2) {
			String imageSorting = ConfigMain.getParameter("ImageSorting", "number");
			s1 = s1.substring(0, s1.lastIndexOf("."));
			s2 = s2.substring(0, s2.lastIndexOf("."));

			if (imageSorting.equalsIgnoreCase("number")) {
				try {
					Integer i1 = Integer.valueOf(s1);
					Integer i2 = Integer.valueOf(s2);
					return i1.compareTo(i2);
				} catch (NumberFormatException e) {
					return s1.compareToIgnoreCase(s2);
				}
			} else if (imageSorting.equalsIgnoreCase("alphanumeric")) {
				return s1.compareToIgnoreCase(s2);
			} else {
				return s1.compareToIgnoreCase(s2);
			}
		}

	}

	/**
	 * 
	 * @param myProzess
	 *            current process
	 * @return sorted list with strings representing images of proces
	 * @throws InvalidImagesException
	 */

	public ArrayList<String> getImageFiles(Prozess myProzess) throws InvalidImagesException {
		File dir;
		try {
			dir = new File(myProzess.getImagesTifDirectory());
			// throw new NullPointerException("wer das liest ist doof");
		} catch (Exception e) {
			throw new InvalidImagesException(e);
		}
		/* Verzeichnis einlesen */
		String[] dateien = dir.list(new Helper().getFilter());
		ArrayList<String> dataList = new ArrayList<String>();
		if (dateien != null && dateien.length > 0) {
			for (int i = 0; i < dateien.length; i++) {
				String s = dateien[i];
				dataList.add(s);
			}
			/* alle Dateien durchlaufen */
			if (dataList != null && dataList.size() != 0) {
				Collections.sort(dataList, new GoobiImageFileComparator());
			}
			return dataList;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param myProzess
	 *            current process
	 * @param directory
	 *            current folder
	 * @return sorted list with strings representing images of proces
	 * @throws InvalidImagesException
	 */

	public ArrayList<String> getImageFiles(Prozess myProzess, String directory) throws InvalidImagesException {
		File dir;
		try {
			dir = new File(myProzess.getImagesDirectory() + directory);
			// throw new NullPointerException("wer das liest ist doof");
		} catch (Exception e) {
			throw new InvalidImagesException(e);
		}
		/* Verzeichnis einlesen */
		String[] dateien = dir.list(new Helper().getFilter());
		ArrayList<String> dataList = new ArrayList<String>();
		if (dateien != null && dateien.length > 0) {
			for (int i = 0; i < dateien.length; i++) {
				String s = dateien[i];
				dataList.add(s);
			}
			/* alle Dateien durchlaufen */
			if (dataList != null && dataList.size() != 0) {
				Collections.sort(dataList, new GoobiImageFileComparator());
			}
			return dataList;
		} else {
			return null;
		}
	}

	/**
	 * {@link FilenameFilter} for all sort of images
	 */

	public static FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			boolean validImage = false;
			// jpeg
			if (name.endsWith("jpg") || name.endsWith("JPG") || name.endsWith("jpeg") || name.endsWith("JPEG")) {
				validImage = true;
			}
			if (name.endsWith(".tif") || name.endsWith(".TIF")) {
				validImage = true;
			}
			// png
			if (name.endsWith(".png") || name.endsWith(".PNG")) {
				validImage = true;
			}
			// gif
			if (name.endsWith(".gif") || name.endsWith(".GIF")) {
				validImage = true;
			}
			// jpeg2000
			if (name.endsWith(".jp2") || name.endsWith(".JP2")) {
				validImage = true;
			}

			return validImage;
		}
	};

}
