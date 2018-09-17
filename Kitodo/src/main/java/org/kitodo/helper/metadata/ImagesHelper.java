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

package org.kitodo.helper.metadata;

import de.sub.goobi.metadaten.Metadaten;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManagerException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;

import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.ugh.ContentFileInterface;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.ReferenceInterface;
import org.kitodo.api.ugh.RomanNumeralInterface;
import org.kitodo.api.ugh.exceptions.ContentFileNotLinkedException;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.DefaultValues;
import org.kitodo.config.enums.Parameter;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.helper.Helper;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.metadata.comparator.MetadataImageComparator;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class ImagesHelper {
    private static final Logger logger = LogManager.getLogger(ImagesHelper.class);
    private final PrefsInterface myPrefs;
    private final DigitalDocumentInterface mydocument;
    private int myLastImage = 0;
    private static final ServiceManager serviceManager = new ServiceManager();
    private static final FileService fileService = serviceManager.getFileService();

    public ImagesHelper(PrefsInterface inPrefs, DigitalDocumentInterface inDocument) {
        this.myPrefs = inPrefs;
        this.mydocument = inDocument;
    }

    /**
     * Markus baut eine Seitenstruktur aus den vorhandenen Images --- Steps -
     * ---- Validation of images compare existing number images with existing
     * number of page DocStructs if it is the same don't do anything if
     * DocStructs are less add new pages to physicalDocStruct if images are less
     * delete pages from the end of pyhsicalDocStruct.
     */
    public void createPagination(Process process, URI directory) throws IOException {
        DocStructInterface physicalStructure = this.mydocument.getPhysicalDocStruct();
        DocStructInterface logicalStructure = this.mydocument.getLogicalDocStruct();
        List<DocStructInterface> allChildren = logicalStructure.getAllChildren();
        while (logicalStructure.getDocStructType().getAnchorClass() != null && Objects.nonNull(allChildren)
                && !allChildren.isEmpty()) {
            logicalStructure = allChildren.get(0);
        }

        // the physical structure tree is only created if it does not exist yet
        if (physicalStructure == null) {
            physicalStructure = createPhysicalStructure(process);
            this.mydocument.setPhysicalDocStruct(physicalStructure);
        }

        if (directory == null) {
            checkIfImagesValid(process.getTitle(),
                serviceManager.getProcessService().getImagesTifDirectory(true, process));
        } else {
            checkIfImagesValid(process.getTitle(),
                    directory);
            // fileService.getProcessSubTypeURI(process, ProcessSubType.IMAGE, null).resolve(directory));
        }

        // retrieve existing pages/images
        DocStructTypeInterface newPage = this.myPrefs.getDocStrctTypeByName("page");
        List<DocStructInterface> oldPages = physicalStructure.getAllChildrenByTypeAndMetadataType("page", "*");
        if (oldPages == null) {
            oldPages = new ArrayList<>();
        }

        // add new page/images if necessary
        if (oldPages.size() == this.myLastImage) {
            return;
        }

        String defaultPagination = ConfigCore.getParameter(Parameter.METS_EDITOR_DEFAULT_PAGINATION,
            Parameter.METS_EDITOR_DEFAULT_PAGINATION_VALUE_UNCOUNTED);
        Map<String, DocStructInterface> assignedImages = new HashMap<>();
        List<DocStructInterface> pageElementsWithoutImages = new ArrayList<>();

        if (physicalStructure.getAllChildren() != null && !physicalStructure.getAllChildren().isEmpty()) {
            for (DocStructInterface page : physicalStructure.getAllChildren()) {
                if (page.getImageName() != null) {
                    URI imageFile;
                    if (directory == null) {
                        imageFile = serviceManager.getProcessService().getImagesTifDirectory(true, process)
                                .resolve(page.getImageName());
                    } else {
                        imageFile = fileService.getProcessSubTypeURI(process, ProcessSubType.IMAGE, null)
                                .resolve(page.getImageName());
                    }
                    if (fileService.fileExist(imageFile)) {
                        assignedImages.put(page.getImageName(), page);
                    } else {
                        try {
                            page.removeContentFile(page.getAllContentFiles().get(0));
                            pageElementsWithoutImages.add(page);
                        } catch (ContentFileNotLinkedException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                } else {
                    pageElementsWithoutImages.add(page);

                }
            }
        }
        List<URI> imagesWithoutPageElements = getImagesWithoutPageElements(process, assignedImages);

        // handle possible cases

        // case 1: existing pages but no images (some images are removed)
        if (!pageElementsWithoutImages.isEmpty() && imagesWithoutPageElements.isEmpty()) {
            for (DocStructInterface pageToRemove : pageElementsWithoutImages) {
                physicalStructure.removeChild(pageToRemove);
                List<ReferenceInterface> refs = new ArrayList<>(pageToRemove.getAllFromReferences());
                for (ReferenceInterface ref : refs) {
                    ref.getSource().removeReferenceTo(pageToRemove);
                }
            }
        } else if (pageElementsWithoutImages.isEmpty() && !imagesWithoutPageElements.isEmpty()) {
            // case 2: no page docs but images (some images are added)
            int currentPhysicalOrder = assignedImages.size();
            for (URI newImage : imagesWithoutPageElements) {
                DocStructInterface dsPage = this.mydocument.createDocStruct(newPage);
                try {
                    // physical page no
                    physicalStructure.addChild(dsPage);
                    dsPage.addMetadata(createMetadataForPhysicalPageNumber(currentPhysicalOrder));

                    // logical page no
                    dsPage.addMetadata(createMetadataForLogicalPageNumber(currentPhysicalOrder, defaultPagination));
                    logicalStructure.addReferenceTo(dsPage, "logical_physical");

                    // image name
                    dsPage.addContentFile(createContentFile(newImage));

                } catch (TypeNotAllowedAsChildException | MetadataTypeNotAllowedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            // case 3: empty page docs and unassinged images
            for (DocStructInterface page : pageElementsWithoutImages) {
                if (!imagesWithoutPageElements.isEmpty()) {
                    // assign new image name to page
                    URI newImageName = imagesWithoutPageElements.get(0);
                    imagesWithoutPageElements.remove(0);
                    page.addContentFile(createContentFile(newImageName));
                } else {
                    // remove page
                    physicalStructure.removeChild(page);
                    List<ReferenceInterface> refs = new ArrayList<>(page.getAllFromReferences());
                    for (ReferenceInterface ref : refs) {
                        ref.getSource().removeReferenceTo(page);
                    }
                }
            }
            if (!imagesWithoutPageElements.isEmpty()) {
                // create new page elements
                int currentPhysicalOrder = physicalStructure.getAllChildren().size();
                for (URI newImage : imagesWithoutPageElements) {
                    DocStructInterface dsPage = this.mydocument.createDocStruct(newPage);
                    try {
                        // physical page no
                        physicalStructure.addChild(dsPage);
                        dsPage.addMetadata(createMetadataForPhysicalPageNumber(currentPhysicalOrder));

                        // logical page no
                        dsPage.addMetadata(createMetadataForLogicalPageNumber(currentPhysicalOrder, defaultPagination));
                        logicalStructure.addReferenceTo(dsPage, "logical_physical");

                        // image name
                        dsPage.addContentFile(createContentFile(newImage));
                    } catch (TypeNotAllowedAsChildException | MetadataTypeNotAllowedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

            }
        }
        int currentPhysicalOrder = 1;
        MetadataTypeInterface mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
        if (physicalStructure.getAllChildrenByTypeAndMetadataType("page", "*") != null) {
            for (DocStructInterface page : physicalStructure.getAllChildrenByTypeAndMetadataType("page", "*")) {
                List<? extends MetadataInterface> pageNoMetadata = page.getAllMetadataByType(mdt);
                if (pageNoMetadata == null || pageNoMetadata.isEmpty()) {
                    currentPhysicalOrder++;
                    break;
                }
                for (MetadataInterface pageNo : pageNoMetadata) {
                    pageNo.setStringValue(String.valueOf(currentPhysicalOrder));
                }
                currentPhysicalOrder++;
            }
        }
    }

    private DocStructInterface createPhysicalStructure(Process process) throws IOException {
        DocStructTypeInterface dst = this.myPrefs.getDocStrctTypeByName("BoundBook");
        DocStructInterface physicalStructure = this.mydocument.createDocStruct(dst);

        // problems with FilePath
        MetadataTypeInterface metadataTypeForPath = this.myPrefs.getMetadataTypeByName("pathimagefiles");
        try {
            MetadataInterface mdForPath = UghImplementation.INSTANCE.createMetadata(metadataTypeForPath);
            URI pathURI = serviceManager.getProcessService().getImagesTifDirectory(false, process);
            String pathString = new File(pathURI).getPath();
            mdForPath.setStringValue(pathString);
            physicalStructure.addMetadata(mdForPath);
        } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
            logger.error(e.getMessage(), e);
        }

        return physicalStructure;
    }

    private List<URI> getImagesWithoutPageElements(Process process, Map<String, DocStructInterface> assignedImages) {
        List<URI> imagesWithoutPageElements = new ArrayList<>();
        try {
            List<URI> imageNamesInMediaFolder = getDataFiles(process);
            for (URI imageName : imageNamesInMediaFolder) {
                if (!assignedImages.containsKey(imageName.getRawPath())) {
                    imagesWithoutPageElements.add(imageName);
                }
            }
        } catch (InvalidImagesException e1) {
            logger.error(e1);
        }
        return imagesWithoutPageElements;
    }

    /**
     * Create Metadata for logical page number.
     *
     * @param currentPhysicalOrder
     *            as int
     * @param defaultPagination
     *            as String
     * @return Metadata object
     */
    private MetadataInterface createMetadataForLogicalPageNumber(int currentPhysicalOrder, String defaultPagination)
            throws MetadataTypeNotAllowedException {
        MetadataTypeInterface metadataType = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        MetadataInterface metadata = UghImplementation.INSTANCE.createMetadata(metadataType);
        metadata.setStringValue(determinePagination(currentPhysicalOrder, defaultPagination));
        return metadata;
    }

    /**
     * Create Metadata for physical page number.
     *
     * @param currentPhysicalOrder
     *            as int
     * @return Metadata object
     */
    private MetadataInterface createMetadataForPhysicalPageNumber(int currentPhysicalOrder)
            throws MetadataTypeNotAllowedException {
        MetadataTypeInterface metadataType = this.myPrefs.getMetadataTypeByName("physPageNumber");
        MetadataInterface metadata = UghImplementation.INSTANCE.createMetadata(metadataType);
        metadata.setStringValue(String.valueOf(++currentPhysicalOrder));
        return metadata;
    }

    /**
     * Create ContentFile with set up location.
     *
     * @param image
     *            URI to image
     * @return ContentFile object
     */
    private ContentFileInterface createContentFile(URI image) {
        ContentFileInterface contentFile = UghImplementation.INSTANCE.createContentFile();
        contentFile.setLocation(image.getPath());
        return contentFile;
    }

    /**
     * Determine pagination for metadata.
     *
     * @param currentPhysicalOrder
     *            as int
     * @param defaultPagination
     *            as String
     * @return pagination value as String
     */
    private String determinePagination(int currentPhysicalOrder, String defaultPagination) {
        if (defaultPagination.equalsIgnoreCase(Parameter.METS_EDITOR_DEFAULT_PAGINATION_VALUE_ARABIC)) {
            return String.valueOf(currentPhysicalOrder);
        } else if (defaultPagination.equalsIgnoreCase(Parameter.METS_EDITOR_DEFAULT_PAGINATION_VALUE_ROMAN)) {
            RomanNumeralInterface roman = UghImplementation.INSTANCE.createRomanNumeral();
            roman.setValue(currentPhysicalOrder);
            return roman.getNumber();
        } else {
            return Parameter.METS_EDITOR_DEFAULT_PAGINATION_VALUE_UNCOUNTED;
        }
    }

    /**
     * scale given image file to png using internal embedded content server.
     */
    public void scaleFile(URI inFileName, URI outFileName, int inSize, int intRotation)
            throws ImageManagerException, IOException, ImageManipulatorException {
        logger.trace("start scaleFile");
        int tmpSize = inSize / 3;
        if (tmpSize < 1) {
            tmpSize = 1;
        }
        logger.trace("tmpSize: {}", tmpSize);
        Optional<String> kitodoContentServerUrl = ConfigCore.getOptionalString(Parameter.KITODO_CONTENT_SERVER_URL);
        if (kitodoContentServerUrl.isPresent()) {
            if (kitodoContentServerUrl.get().isEmpty()) {
                logger.trace("api");
                // TODO source image files are locked under windows forever after
                // converting to png begins.
                ImageManager imageManager = new ImageManager(inFileName.toURL());
                logger.trace("im");
                RenderedImage renderedImage = imageManager.scaleImageByPixel(tmpSize, tmpSize,
                    ImageManager.SCALE_BY_PERCENT, intRotation);
                logger.trace("ri");
                JpegInterpreter jpegInterpreter = new JpegInterpreter(renderedImage);
                logger.trace("pi");
                FileOutputStream outputFileStream = (FileOutputStream) fileService.write(outFileName);
                logger.trace("output");
                jpegInterpreter.writeToStream(null, outputFileStream);
                logger.trace("write stream");
                outputFileStream.flush();
                outputFileStream.close();
                logger.trace("close stream");
            } else {
                String cs = kitodoContentServerUrl.get() + inFileName + "&scale=" + tmpSize + "&rotate=" + intRotation
                    + "&format=jpg";
                cs = cs.replace("\\", "/");
                logger.trace("url: {}", cs);
                URL csUrl = new URL(cs);
                HttpClient httpclient = new HttpClient();
                GetMethod method = new GetMethod(csUrl.toString());
                logger.trace("get");
                Integer contentServerTimeOut = ConfigCore.getIntParameter(Parameter.KITODO_CONTENT_SERVER_TIMEOUT,
                    DefaultValues.KITODO_CONTENT_SERVER_TIMEOUT);
                method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);
                int statusCode = httpclient.executeMethod(method);
                if (statusCode != HttpStatus.SC_OK) {
                    return;
                }
                logger.trace("statusCode: {}", statusCode);
                InputStream inStream = method.getResponseBodyAsStream();
                logger.trace("inStream");
                try (BufferedInputStream bis = new BufferedInputStream(inStream);
                     OutputStream fos = fileService.write(outFileName)) {
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
                inStream.close();
            }
            logger.trace("end scaleFile");
        }
    }

    // Add a method to validate the image files

    /**
     * Die Images eines Prozesses auf Vollständigkeit prüfen.
     */
    public boolean checkIfImagesValid(String title, URI folder) {
        boolean isValid = true;
        this.myLastImage = 0;

        /*
         * alle Bilder durchlaufen und dafür die Seiten anlegen
         */
        if (fileService.fileExist(folder)) {
            List<URI> files = fileService.getSubUris(Helper.dataFilter, folder);
            if (files.isEmpty()) {
                Helper.setErrorMessage("[" + title + "] No objects found");
                return false;
            }

            this.myLastImage = files.size();
            if (ConfigCore.getParameter(Parameter.IMAGE_PREFIX, DefaultValues.IMAGE_PREFIX)
                    .equals("\\d{8}")) {
                Collections.sort(files);
                int counter = 1;
                int myDiff = 0;
                String currentFileName = null;
                try {
                    for (Iterator<URI> iterator = files.iterator(); iterator.hasNext(); counter++) {
                        currentFileName = fileService.getFileName(iterator.next());
                        int curFileNumber = Integer.parseInt(currentFileName);
                        if (curFileNumber != counter + myDiff) {
                            Helper.setErrorMessage("[" + title + "] expected Image " + (counter + myDiff)
                                    + " but found File " + currentFileName);
                            myDiff = curFileNumber - counter;
                            isValid = false;
                        }
                    }
                } catch (NumberFormatException e1) {
                    isValid = false;
                    Helper.setErrorMessage(
                        "[" + title + "] Filename of image wrong - not an 8-digit-number: " + currentFileName);
                }
                return isValid;
            }
            return true;
        }
        Helper.setErrorMessage("[" + title + "] No image-folder found");
        return false;
    }

    /**
     * Get image files.
     *
     * @param directory
     *            current folder
     * @return sorted list with strings representing images of process
     */
    public List<URI> getImageFiles(URI directory) {
        /* Verzeichnis einlesen */
        List<URI> files = fileService.getSubUris(Helper.imageNameFilter, directory);
        ArrayList<URI> finalFiles = new ArrayList<>();
        for (URI file : files) {
            String newURI = file.toString().replace(directory.toString(), "");
            finalFiles.add(URI.create(newURI));
        }

        List<URI> dataList = new ArrayList<>(finalFiles);

        if (!dataList.isEmpty()) {
            List<URI> orderedFileNameList = prepareOrderedFileNameList(dataList);

            if (orderedFileNameList.size() == dataList.size()) {
                return orderedFileNameList;
            } else {
                dataList.sort(new MetadataImageComparator());
                return dataList;
            }
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get image files.
     *
     * @param physical
     *            DocStruct object
     * @return list of Strings
     */
    public List<URI> getImageFiles(DocStructInterface physical) {
        List<URI> orderedFileList = new ArrayList<>();
        List<DocStructInterface> pages = physical.getAllChildren();
        if (pages != null) {
            for (DocStructInterface page : pages) {
                URI filename = URI.create(page.getImageName());
                orderedFileList.add(filename);
            }
        }
        return orderedFileList;
    }

    private List<URI> prepareOrderedFileNameList(List<URI> dataList) {
        List<URI> orderedFileNameList = new ArrayList<>();
        List<DocStructInterface> pagesList = mydocument.getPhysicalDocStruct().getAllChildren();
        if (pagesList != null) {
            for (DocStructInterface page : pagesList) {
                String fileName = page.getImageName();
                String fileNamePrefix = fileName.replace("." + Metadaten.getFileExtension(fileName), "");
                for (URI currentImage : dataList) {
                    String currentFileName = fileService.getFileName(currentImage);
                    if (currentFileName.equals(fileNamePrefix)) {
                        orderedFileNameList.add(currentImage);
                        break;
                    }
                }
            }
        }
        return orderedFileNameList;
    }

    /**
     * Get data files. First read them all and next if their size is bigger than
     * zero sort them with use of GoobiImageFileComparator.
     *
     * @param process
     *            Process object
     * @return list of URIs
     */
    public List<URI> getDataFiles(Process process) throws InvalidImagesException {
        URI dir;
        try {
            dir = serviceManager.getProcessService().getImagesTifDirectory(true, process);
        } catch (IOException | RuntimeException e) {
            throw new InvalidImagesException(e);
        }
        /* Verzeichnis einlesen */
        ArrayList<URI> dataList = new ArrayList<>();
        List<URI> files = fileService.getSubUris(Helper.dataFilter, dir);
        if (!files.isEmpty()) {
            dataList.addAll(files);
            dataList.sort(new MetadataImageComparator());
        }
        return dataList;
    }

}
