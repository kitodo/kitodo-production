package de.sub.goobi.Metadaten;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
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
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import de.sub.goobi.Beans.Prozess;
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
		this.myPrefs = inPrefs;
		this.mydocument = inDocument;
	}

	/**
	 * Markus baut eine Seitenstruktur aus den vorhandenen Images
	 * ---------------- Steps - ---------------- Validation of images compare
	 * existing number images with existing number of page DocStructs if it is
	 * the same don't do anything if DocStructs are less add new pages to
	 * physicalDocStruct if images are less delete pages from the end of
	 * pyhsicalDocStruct --------------------------------
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
	public void createPagination(Prozess inProzess, String directory) throws TypeNotAllowedForParentException, IOException, InterruptedException, SwapException,
			DAOException {
		DocStruct physicaldocstruct = this.mydocument.getPhysicalDocStruct();

		DocStruct log = this.mydocument.getLogicalDocStruct();
		if (log.getType().isAnchor()) {
			if (log.getAllChildren() != null && log.getAllChildren().size() > 0) {
				log = log.getAllChildren().get(0);
			}
		}
		
		/*-------------------------------- 
		 * der physische Baum wird nur
		 * angelegt, wenn er noch nicht existierte
		 * --------------------------------*/
		if (physicaldocstruct == null) {
			DocStructType dst = this.myPrefs.getDocStrctTypeByName("BoundBook");
			physicaldocstruct = this.mydocument.createDocStruct(dst);

			/*-------------------------------- 
			 * Probleme mit dem FilePath
			 * -------------------------------- */
			MetadataType MDTypeForPath = this.myPrefs.getMetadataTypeByName("pathimagefiles");
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
			this.mydocument.setPhysicalDocStruct(physicaldocstruct);
		}

		if (directory==null){
			checkIfImagesValid(inProzess.getTitel(), inProzess.getImagesTifDirectory());			
		}else{
			checkIfImagesValid(inProzess.getTitel(), inProzess.getImagesDirectory() + directory);
		}
		

		/*------------------------------- 
		 * retrieve existing pages/images
		 * -------------------------------*/
		DocStructType newPage = this.myPrefs.getDocStrctTypeByName("page");
		List<DocStruct> oldPages = physicaldocstruct.getAllChildrenByTypeAndMetadataType("page", "*");
		if (oldPages == null) {
			oldPages = new ArrayList<DocStruct>();
		}

		/*-------------------------------- 
		 * add new page/images if necessary
		 * --------------------------------*/
		if (oldPages.size() < this.myLastImage) {
			for (int i = oldPages.size(); i < this.myLastImage; i++) {
				DocStruct dsPage = this.mydocument.createDocStruct(newPage);
				try {

					/*
					 * -------------------------------- die physischen Seiten
					 * anlegen, sind nicht änderbar für den Benutzer
					 * --------------------------------
					 */
					physicaldocstruct.addChild(dsPage);
					MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
					Metadata mdTemp = new Metadata(mdt);
					// mdTemp.setType(mdt);
					mdTemp.setValue(String.valueOf(i + 1));
					dsPage.addMetadata(mdTemp);
					
					/*
					 * -------------------------------- die logischen
					 * Seitennummern anlegen, die der Benutzer auch ändern kann
					 * --------------------------------
					 */
					mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
					mdTemp = new Metadata(mdt);
					// mdTemp.setType(mdt);
					mdTemp.setValue(String.valueOf(i + 1));
					dsPage.addMetadata(mdTemp);
					log.addReferenceTo(dsPage, "logical_physical");
							
							
					// myLogger.debug("fertig mit Paginierung für Nr. " + i +
					// " von " + myBildLetztes);
				} catch (TypeNotAllowedAsChildException e) {
					logger.error(e);
				} catch (MetadataTypeNotAllowedException e) {
					logger.error(e);
				}
			}
		}

		else if (oldPages.size() > this.myLastImage) {
			MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
			for (DocStruct page : oldPages) {
				List<? extends Metadata> mdts = page.getAllMetadataByType(mdt);
				if (mdts.size() != 1) {
					throw new SwapException("found page DocStruct with more or less than 1 pysical pagination");
					// return myLastImage;
				}
				/*
				 * delete page DocStruct, if physical pagenumber higher than
				 * last imagenumber
				 */
				if (Integer.parseInt(mdts.get(0).getValue()) > this.myLastImage) {
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
		logger.trace("start scaleFile");
		int tmpSize = inSize / 3;
		if (tmpSize < 1) {
			tmpSize = 1;
		}
		logger.trace("tmpSize: " + tmpSize);
		if (ConfigMain.getParameter("ContentServerUrl") == null) {
			logger.trace("api");
			ImageManager im = new ImageManager(new File(inFileName).toURI().toURL());
			logger.trace("im");
			RenderedImage ri = im.scaleImageByPixel(tmpSize, tmpSize, ImageManager.SCALE_BY_PERCENT, intRotation);
			logger.trace("ri");
			JpegInterpreter pi = new JpegInterpreter(ri);
			logger.trace("pi");
			FileOutputStream outputFileStream = new FileOutputStream(outFileName);
			logger.trace("output");
			pi.writeToStream(null, outputFileStream);
			logger.trace("write stream");
			outputFileStream.close();
			logger.trace("close stream");
		} else {
			String cs = ConfigMain.getParameter("ContentServerUrl") + inFileName + "&scale=" + tmpSize + "&rotate=" + intRotation + "&format=jpg";
			cs = cs.replace("\\", "/");
			logger.trace("url: " + cs);
			URL csUrl = new URL(cs);
			HttpClient httpclient = new HttpClient();
			GetMethod method = new GetMethod(csUrl.toString());
			logger.trace("get");
			Integer contentServerTimeOut = ConfigMain.getIntParameter("goobiContentServerTimeOut", 60000);
			method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);
			int statusCode = httpclient.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				return;
			}
			logger.trace("statusCode: " + statusCode);
			InputStream inStream = method.getResponseBodyAsStream();
			logger.trace("inStream");
			BufferedInputStream bis = new BufferedInputStream(inStream);
			logger.trace("BufferedInputStream");
			FileOutputStream fos = new FileOutputStream(outFileName);
			logger.trace("FileOutputStream");
			byte[] bytes = new byte[8192];
			int count = bis.read(bytes);
			while (count != -1 && count <= 8192) {
				fos.write(bytes, 0, count);
				count = bis.read(bytes);
			}
			if (count != -1) {
				fos.write(bytes, 0, count);
			}
			logger.trace("write");
			fos.close();
			bis.close();
		}
		logger.trace("end scaleFile");
	}

	// Add a method to validate the image files

	/**
	 * die Images eines Prozesses auf Vollständigkeit prüfen
	 * ================================================================
	 * 
	 * @throws DAOException
	 * @throws SwapException
	 */
	public boolean checkIfImagesValid(String title, String folder) throws IOException, InterruptedException, SwapException, DAOException {
		boolean isValid = true;
		this.myLastImage = 0;

		/*-------------------------------- 
		 * alle Bilder durchlaufen und dafür
		 * die Seiten anlegen 
		 * --------------------------------*/
		File dir = new File(folder);
		if (dir.exists()) {
			String[] dateien = dir.list(Helper.dataFilter);
			if (dateien == null || dateien.length == 0) {
				Helper.setFehlerMeldung("[" + title + "] No images found");
				return false;
			}

			// ArrayList<String> images = getImageFiles(inProzess);
			this.myLastImage = dateien.length;
			if (ConfigMain.getParameter("ImagePrefix", "\\d{8}").equals("\\d{8}")) {
				List<String> filesDirs = Arrays.asList(dateien);
				Collections.sort(filesDirs);
				int counter = 1;
				int myDiff = 0;
				String curFile = null;
				try {
					for (Iterator<String> iterator = filesDirs.iterator(); iterator.hasNext(); counter++) {
						curFile = iterator.next();
						int curFileNumber = Integer.parseInt(curFile.substring(0, curFile.indexOf(".")));
						if (curFileNumber != counter + myDiff) {
							Helper.setFehlerMeldung("[" + title + "] expected Image " + (counter + myDiff) + " but found File "
									+ curFile);
							myDiff = curFileNumber - counter;
							isValid = false;
						}
					}
				} catch (NumberFormatException e1) {
					isValid = false;
					Helper.setFehlerMeldung("[" + title + "] Filename of image wrong - not an 8-digit-number: " + curFile);
				}
				return isValid;
			}
			return true;
		}
		Helper.setFehlerMeldung("[" + title + "] No image-folder found");
		return false;
	}

	public static class GoobiImageFileComparator implements Comparator<String> {

		@Override
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
		String[] dateien = dir.list(Helper.imageNameFilter);
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

	public List<String> getDataFiles(Prozess myProzess) throws InvalidImagesException {
		File dir;
		try {
			dir = new File(myProzess.getImagesTifDirectory());
			// throw new NullPointerException("wer das liest ist doof");
		} catch (Exception e) {
			throw new InvalidImagesException(e);
		}
		/* Verzeichnis einlesen */
		String[] dateien = dir.list(Helper.dataFilter);
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
		String[] dateien = dir.list(Helper.imageNameFilter);
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

//	/**
//	 * {@link FilenameFilter} for all sort of images
//	 */
//
//	public static FilenameFilter filter = new FilenameFilter() {
//		@Override
//		public boolean accept(File dir, String name) {
//			boolean validImage = false;
//			// jpeg
//			if (name.endsWith("jpg") || name.endsWith("JPG") || name.endsWith("jpeg") || name.endsWith("JPEG")) {
//				validImage = true;
//			}
//			if (name.endsWith(".tif") || name.endsWith(".TIF")) {
//				validImage = true;
//			}
//			// png
//			if (name.endsWith(".png") || name.endsWith(".PNG")) {
//				validImage = true;
//			}
//			// gif
//			if (name.endsWith(".gif") || name.endsWith(".GIF")) {
//				validImage = true;
//			}
//			// jpeg2000
//			if (name.endsWith(".jp2") || name.endsWith(".JP2")) {
//				validImage = true;
//			}
//
//			return validImage;
//		}
//	};

}
