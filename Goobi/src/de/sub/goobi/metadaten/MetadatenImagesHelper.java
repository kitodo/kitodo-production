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

package de.sub.goobi.metadaten;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.dl.RomanNumeral;
import ugh.exceptions.ContentFileNotLinkedException;
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
    private final Prefs myPrefs;
    private final DigitalDocument mydocument;
    private int myLastImage = 0;

    public MetadatenImagesHelper(Prefs inPrefs, DigitalDocument inDocument) {
        this.myPrefs = inPrefs;
        this.mydocument = inDocument;
    }

    /**
     * Markus baut eine Seitenstruktur aus den vorhandenen Images ---------------- Steps - ---------------- Validation of images compare existing
     * number images with existing number of page DocStructs if it is the same don't do anything if DocStructs are less add new pages to
     * physicalDocStruct if images are less delete pages from the end of pyhsicalDocStruct --------------------------------
     *
     * @throws TypeNotAllowedForParentException
     * @throws TypeNotAllowedForParentException
     * @throws InterruptedException
     * @throws IOException
     * @throws InterruptedException
     * @throws IOException
     * @throws DAOException
     * @throws SwapException
     */
    public void createPagination(Prozess inProzess, String directory) throws TypeNotAllowedForParentException, IOException, InterruptedException,
            SwapException, DAOException {
        DocStruct physicaldocstruct = this.mydocument.getPhysicalDocStruct();

        DocStruct log = this.mydocument.getLogicalDocStruct();
        while (log.getType().getAnchorClass() != null && log.getAllChildren() != null
                && log.getAllChildren().size() > 0) {
            log = log.getAllChildren().get(0);
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
                if (SystemUtils.IS_OS_WINDOWS) {
                    mdForPath.setValue("file:/" + inProzess.getImagesTifDirectory(false));
                } else {
                    mdForPath.setValue("file://" + inProzess.getImagesTifDirectory(false));
                }
                physicaldocstruct.addMetadata(mdForPath);
            } catch (MetadataTypeNotAllowedException e1) {
            } catch (DocStructHasNoTypeException e1) {
            }
            this.mydocument.setPhysicalDocStruct(physicaldocstruct);
        }

        if (directory == null) {
            checkIfImagesValid(inProzess.getTitel(), inProzess.getImagesTifDirectory(true));
        } else {
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

        if (oldPages.size() == this.myLastImage) {
            return;
        }

        String defaultPagination = ConfigMain.getParameter("MetsEditorDefaultPagination", "uncounted");
        Map<String, DocStruct> assignedImages = new HashMap<String, DocStruct>();
        List<DocStruct> pageElementsWithoutImages = new ArrayList<DocStruct>();
        List<String> imagesWithoutPageElements = new ArrayList<String>();

        if (physicaldocstruct.getAllChildren() != null && !physicaldocstruct.getAllChildren().isEmpty()) {
            for (DocStruct page : physicaldocstruct.getAllChildren()) {
                if (page.getImageName() != null) {
                    File imageFile = null;
                    if (directory == null) {
                        imageFile = new File(inProzess.getImagesTifDirectory(true), page.getImageName());
                    } else {
                        imageFile = new File(inProzess.getImagesDirectory() + directory, page.getImageName());
                    }
                    if (imageFile.exists()) {
                        assignedImages.put(page.getImageName(), page);
                    } else {
                        try {
                            page.removeContentFile(page.getAllContentFiles().get(0));
                            pageElementsWithoutImages.add(page);
                        } catch (ContentFileNotLinkedException e) {
                            logger.error(e);
                        }
                    }
                } else {
                    pageElementsWithoutImages.add(page);

                }
            }

        }
        try {
            List<String> imageNamesInMediaFolder = getDataFiles(inProzess);
            if (imageNamesInMediaFolder != null) {
                for (String imageName : imageNamesInMediaFolder) {
                    if (!assignedImages.containsKey(imageName)) {
                        imagesWithoutPageElements.add(imageName);
                    }
                }
            }
        } catch (InvalidImagesException e1) {
            logger.error(e1);
        }

        // handle possible cases

        // case 1: existing pages but no images (some images are removed)
        if (!pageElementsWithoutImages.isEmpty() && imagesWithoutPageElements.isEmpty()) {
            for (DocStruct pageToRemove : pageElementsWithoutImages) {
                physicaldocstruct.removeChild(pageToRemove);
                List<Reference> refs = new ArrayList<Reference>(pageToRemove.getAllFromReferences());
                for (ugh.dl.Reference ref : refs) {
                    ref.getSource().removeReferenceTo(pageToRemove);
                }
            }
        }

        // case 2: no page docs but images (some images are added)
        else if (pageElementsWithoutImages.isEmpty() && !imagesWithoutPageElements.isEmpty()) {
            int currentPhysicalOrder = assignedImages.size();
            for (String newImage : imagesWithoutPageElements) {
                DocStruct dsPage = this.mydocument.createDocStruct(newPage);
                try {
                    // physical page no
                    physicaldocstruct.addChild(dsPage);
                    MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
                    Metadata mdTemp = new Metadata(mdt);
                    mdTemp.setValue(String.valueOf(++currentPhysicalOrder));
                    dsPage.addMetadata(mdTemp);

                    // logical page no
                    mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
                    mdTemp = new Metadata(mdt);

                    if (defaultPagination.equalsIgnoreCase("arabic")) {
                        mdTemp.setValue(String.valueOf(currentPhysicalOrder));
                    } else if (defaultPagination.equalsIgnoreCase("roman")) {
                        RomanNumeral roman = new RomanNumeral();
                        roman.setValue(currentPhysicalOrder);
                        mdTemp.setValue(roman.getNumber());
                    } else {
                        mdTemp.setValue("uncounted");
                    }

                    dsPage.addMetadata(mdTemp);
                    log.addReferenceTo(dsPage, "logical_physical");

                    // image name
                    ContentFile cf = new ContentFile();
                    if (SystemUtils.IS_OS_WINDOWS) {
                        cf.setLocation("file:/" + inProzess.getImagesTifDirectory(false) + newImage);
                    } else {
                        cf.setLocation("file://" + inProzess.getImagesTifDirectory(false) + newImage);
                    }
                    dsPage.addContentFile(cf);

                } catch (TypeNotAllowedAsChildException e) {
                    logger.error(e);
                } catch (MetadataTypeNotAllowedException e) {
                    logger.error(e);
                }
            }
        }

        // case 3: empty page docs and unassinged images
        else {
            for (DocStruct page : pageElementsWithoutImages) {
                if (!imagesWithoutPageElements.isEmpty()) {
                    // assign new image name to page
                    String newImageName = imagesWithoutPageElements.get(0);
                    imagesWithoutPageElements.remove(0);
                    ContentFile cf = new ContentFile();
                    if (SystemUtils.IS_OS_WINDOWS) {
                        cf.setLocation("file:/" + inProzess.getImagesTifDirectory(false) + newImageName);
                    } else {
                        cf.setLocation("file://" + inProzess.getImagesTifDirectory(false) + newImageName);
                    }
                    page.addContentFile(cf);
                } else {
                    // remove page
                    physicaldocstruct.removeChild(page);
                    List<Reference> refs = new ArrayList<Reference>(page.getAllFromReferences());
                    for (ugh.dl.Reference ref : refs) {
                        ref.getSource().removeReferenceTo(page);
                    }
                }
            }
            if (!imagesWithoutPageElements.isEmpty()) {
                // create new page elements

                int currentPhysicalOrder = physicaldocstruct.getAllChildren().size();
                for (String newImage : imagesWithoutPageElements) {
                    DocStruct dsPage = this.mydocument.createDocStruct(newPage);
                    try {
                        // physical page no
                        physicaldocstruct.addChild(dsPage);
                        MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
                        Metadata mdTemp = new Metadata(mdt);
                        mdTemp.setValue(String.valueOf(++currentPhysicalOrder));
                        dsPage.addMetadata(mdTemp);

                        // logical page no
                        mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
                        mdTemp = new Metadata(mdt);

                        if (defaultPagination.equalsIgnoreCase("arabic")) {
                            mdTemp.setValue(String.valueOf(currentPhysicalOrder));
                        } else if (defaultPagination.equalsIgnoreCase("roman")) {
                            RomanNumeral roman = new RomanNumeral();
                            roman.setValue(currentPhysicalOrder);
                            mdTemp.setValue(roman.getNumber());
                        } else {
                            mdTemp.setValue("uncounted");
                        }

                        dsPage.addMetadata(mdTemp);
                        log.addReferenceTo(dsPage, "logical_physical");

                        // image name
                        ContentFile cf = new ContentFile();
                        if (SystemUtils.IS_OS_WINDOWS) {
                            cf.setLocation("file:/" + inProzess.getImagesTifDirectory(false) + newImage);
                        } else {
                            cf.setLocation("file://" + inProzess.getImagesTifDirectory(false) + newImage);
                        }
                        dsPage.addContentFile(cf);

                    } catch (TypeNotAllowedAsChildException e) {
                        logger.error(e);
                    } catch (MetadataTypeNotAllowedException e) {
                        logger.error(e);
                    }
                }

            }
        }
        int currentPhysicalOrder = 1;
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
        if (physicaldocstruct.getAllChildrenByTypeAndMetadataType("page", "*") != null) {
            for (DocStruct page : physicaldocstruct.getAllChildrenByTypeAndMetadataType("page", "*")) {
                List<? extends Metadata> pageNoMetadata = page.getAllMetadataByType(mdt);
                if (pageNoMetadata == null || pageNoMetadata.size() == 0) {
                    currentPhysicalOrder++;
                    break;
                }
                for (Metadata pageNo : pageNoMetadata) {
                    pageNo.setValue(String.valueOf(currentPhysicalOrder));
                }
                currentPhysicalOrder++;
            }
        }
    }

    /**
     * scale given image file using internal embedded content server in jpg format
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
        if(logger.isTraceEnabled()){
            logger.trace("tmpSize: " + tmpSize);
        }
        if (ConfigMain.getParameter("goobiContentServerUrl", "").equals("")) {
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
            String cs = ConfigMain.getParameter("goobiContentServerUrl") + inFileName + "&scale=" + tmpSize + "&rotate=" + intRotation + "&format=jpg";
            cs = cs.replace("\\", "/");
            if(logger.isTraceEnabled()){
                logger.trace("url: " + cs);
            }
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
            if(logger.isTraceEnabled()){
                logger.trace("statusCode: " + statusCode);
            }
            InputStream inStream = method.getResponseBodyAsStream();
            logger.trace("inStream");
            try (
                BufferedInputStream bis = new BufferedInputStream(inStream);
                FileOutputStream fos = new FileOutputStream(outFileName);
            ) {
                logger.trace("BufferedInputStream");
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
            }
            logger.trace("write");
        }
        logger.trace("end scaleFile");
    }

    // Add a method to validate the image files

    /**
     * die Images eines Prozesses auf Vollständigkeit prüfen ================================================================
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
                Helper.setFehlerMeldung("[" + title + "] No objects found");
                return false;
            }

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
                            Helper.setFehlerMeldung("[" + title + "] expected Image " + (counter + myDiff) + " but found File " + curFile);
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
     * @param myProzess current process
     * @return sorted list with strings representing images of process
     * @throws InvalidImagesException
     */

    public ArrayList<String> getImageFiles(Prozess myProzess) throws InvalidImagesException {
        File dir;
        try {
            dir = new File(myProzess.getImagesTifDirectory(true));
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
            if (dataList.size() != 0) {
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
            dir = new File(myProzess.getImagesTifDirectory(true));
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
            if (dataList.size() != 0) {
                Collections.sort(dataList, new GoobiImageFileComparator());
            }
            return dataList;
        } else {
            return null;
        }
    }

    /**
     *
     * @param myProzess current process
     * @param directory current folder
     * @return sorted list with strings representing images of process
     * @throws InvalidImagesException
     */

    public List<String> getImageFiles(Prozess myProzess, String directory) throws InvalidImagesException {
        File dir;
        try {
            dir = new File(myProzess.getImagesDirectory() + directory);
        } catch (Exception e) {
            throw new InvalidImagesException(e);
        }
        /* Verzeichnis einlesen */
        String[] dateien = dir.list(Helper.imageNameFilter);
        List<String> dataList = new ArrayList<String>();
        if (dateien != null && dateien.length > 0) {
            for (int i = 0; i < dateien.length; i++) {
                String s = dateien[i];
                dataList.add(s);
            }
            /* alle Dateien durchlaufen */
        }
        List<String> orderedFilenameList = new ArrayList<String>();
        if (dataList.size() != 0) {
            List<DocStruct> pagesList = mydocument.getPhysicalDocStruct().getAllChildren();
            if (pagesList != null) {
                for (DocStruct page : pagesList) {
                    String filename = page.getImageName();
                    String filenamePrefix = filename.replace(Metadaten.getFileExtension(filename), "");
                    for (String currentImage : dataList) {
                        String currentImagePrefix = currentImage.replace(Metadaten.getFileExtension(currentImage), "");
                        if (currentImagePrefix.equals(filenamePrefix)) {
                            orderedFilenameList.add(currentImage);
                            break;
                        }
                    }
                }
                //                    orderedFilenameList.add(page.getImageName());
            }

            if (orderedFilenameList.size() == dataList.size()) {
                return orderedFilenameList;

            } else {
                Collections.sort(dataList, new GoobiImageFileComparator());
                return dataList;
            }
        } else {
            return null;
        }
    }

    public List<String> getImageFiles(DocStruct physical) {
        List<String> orderedFileList = new ArrayList<String>();
        List<DocStruct> pages = physical.getAllChildren();
        if (pages != null) {
            for (DocStruct page : pages) {
                String filename = page.getImageName();
                if (filename != null) {
                    orderedFileList.add(filename);
                } else {
                    logger.error("cannot find image");
                }
            }
        }
        return orderedFileList;
    }

}
