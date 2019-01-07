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

package org.kitodo.production.helper.metadata;

import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManagerException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;

import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
import org.kitodo.api.ugh.DigitalDocumentInterface;
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
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyContentFileHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyRomanNumeralHelper;
import org.kitodo.production.metadata.MetadataProcessor;
import org.kitodo.production.metadata.comparator.MetadataImageComparator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class ImageHelper {
    private static final Logger logger = LogManager.getLogger(ImageHelper.class);
    private final PrefsInterface myPrefs;
    private final DigitalDocumentInterface mydocument;
    private int myLastImage = 0;
    private static final FileService fileService = ServiceManager.getFileService();

    public ImageHelper(PrefsInterface inPrefs, DigitalDocumentInterface inDocument) {
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
        LegacyDocStructHelperInterface physicalStructure = this.mydocument.getPhysicalDocStruct();
        LegacyDocStructHelperInterface logicalStructure = this.mydocument.getLogicalDocStruct();
        List<LegacyDocStructHelperInterface> allChildren = logicalStructure.getAllChildren();
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
            checkIfImagesValid(process.getTitle(), ServiceManager.getProcessService().getImagesTifDirectory(true,
                process.getId(), process.getTitle(), process.getProcessBaseUri()));
        } else {
            checkIfImagesValid(process.getTitle(), directory);
            // fileService.getProcessSubTypeURI(process, ProcessSubType.IMAGE, null).resolve(directory));
        }

        // retrieve existing pages/images
        DocStructTypeInterface newPage = this.myPrefs.getDocStrctTypeByName("page");
        List<LegacyDocStructHelperInterface> oldPages = physicalStructure.getAllChildrenByTypeAndMetadataType("page", "*");
        if (oldPages == null) {
            oldPages = new ArrayList<>();
        }

        // add new page/images if necessary
        if (oldPages.size() == this.myLastImage) {
            return;
        }

        String defaultPagination = ConfigCore.getParameterOrDefaultValue(ParameterCore.METS_EDITOR_DEFAULT_PAGINATION);
        Map<String, LegacyDocStructHelperInterface> assignedImages = new HashMap<>();
        List<LegacyDocStructHelperInterface> pageElementsWithoutImages = new ArrayList<>();

        if (physicalStructure.getAllChildren() != null && !physicalStructure.getAllChildren().isEmpty()) {
            for (LegacyDocStructHelperInterface page : physicalStructure.getAllChildren()) {
                if (page.getImageName() != null) {
                    URI imageFile;
                    if (directory == null) {
                        imageFile = ServiceManager.getProcessService().getImagesTifDirectory(true, process.getId(),
                            process.getTitle(), process.getProcessBaseUri()).resolve(page.getImageName());
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
            for (LegacyDocStructHelperInterface pageToRemove : pageElementsWithoutImages) {
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
                LegacyDocStructHelperInterface dsPage = this.mydocument.createDocStruct(newPage);
                try {
                    // physical page no
                    physicalStructure.addChild(dsPage);
                    currentPhysicalOrder++;
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
            for (LegacyDocStructHelperInterface page : pageElementsWithoutImages) {
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
                    LegacyDocStructHelperInterface dsPage = this.mydocument.createDocStruct(newPage);
                    try {
                        // physical page no
                        physicalStructure.addChild(dsPage);
                        currentPhysicalOrder++;
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
            for (LegacyDocStructHelperInterface page : physicalStructure.getAllChildrenByTypeAndMetadataType("page", "*")) {
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
        Optional<String> kitodoContentServerUrl = ConfigCore.getOptionalString(ParameterCore.KITODO_CONTENT_SERVER_URL);
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
                Integer contentServerTimeOut = ConfigCore
                        .getIntParameterOrDefaultValue(ParameterCore.KITODO_CONTENT_SERVER_TIMEOUT);
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
            List<URI> files = fileService.getSubUris(dataFilter, folder);
            if (files.isEmpty()) {
                Helper.setErrorMessage("[" + title + "] No objects found");
                return false;
            }

            this.myLastImage = files.size();
            if (ConfigCore.getParameterOrDefaultValue(ParameterCore.IMAGE_PREFIX).equals("\\d{8}")) {
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
        List<URI> files = fileService.getSubUris(imageNameFilter, directory);
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
    public List<URI> getImageFiles(LegacyDocStructHelperInterface physical) {
        List<URI> orderedFileList = new ArrayList<>();
        List<LegacyDocStructHelperInterface> pages = physical.getAllChildren();
        if (pages != null) {
            for (LegacyDocStructHelperInterface page : pages) {
                URI filename = URI.create(page.getImageName());
                orderedFileList.add(filename);
            }
        }
        return orderedFileList;
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
            dir = ServiceManager.getProcessService().getImagesTifDirectory(true, process.getId(), process.getTitle(),
                process.getProcessBaseUri());
        } catch (IOException | RuntimeException e) {
            throw new InvalidImagesException(e);
        }
        /* Verzeichnis einlesen */
        ArrayList<URI> dataList = new ArrayList<>();
        List<URI> files = fileService.getSubUris(dataFilter, dir);
        if (!files.isEmpty()) {
            dataList.addAll(files);
            dataList.sort(new MetadataImageComparator());
        }
        return dataList;
    }

    public static final FilenameFilter imageNameFilter = (dir, name) -> {
        List<String> regexList = getImageNameRegexList();

        for (String regex : regexList) {
            if (name.matches(regex)) {
                return true;
            }
        }

        return false;
    };

    public static final FilenameFilter dataFilter = (dir, name) -> {
        List<String> regexList = getDataRegexList();

        for (String regex : regexList) {
            if (name.matches(regex)) {
                return true;
            }
        }

        return false;
    };

    private List<URI> prepareOrderedFileNameList(List<URI> dataList) {
        List<URI> orderedFileNameList = new ArrayList<>();
        List<LegacyDocStructHelperInterface> pagesList = mydocument.getPhysicalDocStruct().getAllChildren();
        if (pagesList != null) {
            for (LegacyDocStructHelperInterface page : pagesList) {
                String fileName = page.getImageName();
                String fileNamePrefix = fileName.replace("." + MetadataProcessor.getFileExtension(fileName), "");
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

    private LegacyDocStructHelperInterface createPhysicalStructure(Process process) throws IOException {
        DocStructTypeInterface dst = this.myPrefs.getDocStrctTypeByName("BoundBook");
        LegacyDocStructHelperInterface physicalStructure = this.mydocument.createDocStruct(dst);

        // problems with FilePath
        MetadataTypeInterface metadataTypeForPath = this.myPrefs.getMetadataTypeByName("pathimagefiles");
        try {
            MetadataInterface mdForPath = new LegacyMetadataHelper(metadataTypeForPath);
            URI pathURI = ServiceManager.getProcessService().getImagesTifDirectory(false, process.getId(),
                process.getTitle(), process.getProcessBaseUri());
            String pathString = new File(pathURI).getPath();
            mdForPath.setStringValue(pathString);
            physicalStructure.addMetadata(mdForPath);
        } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
            logger.error(e.getMessage(), e);
        }

        return physicalStructure;
    }

    private List<URI> getImagesWithoutPageElements(Process process, Map<String, LegacyDocStructHelperInterface> assignedImages) {
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
        MetadataInterface metadata = new LegacyMetadataHelper(metadataType);
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
        MetadataInterface metadata = new LegacyMetadataHelper(metadataType);
        metadata.setStringValue(String.valueOf(currentPhysicalOrder));
        return metadata;
    }

    /**
     * Create ContentFile with set up location.
     *
     * @param image
     *            URI to image
     * @return ContentFile object
     */
    private LegacyContentFileHelper createContentFile(URI image) {
        LegacyContentFileHelper contentFile = new LegacyContentFileHelper();
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
        if (defaultPagination.equalsIgnoreCase(
            (String) ParameterCore.METS_EDITOR_DEFAULT_PAGINATION.getParameter().getPossibleValues().get(0))) {
            return String.valueOf(currentPhysicalOrder);
        } else if (defaultPagination.equalsIgnoreCase(
            (String) ParameterCore.METS_EDITOR_DEFAULT_PAGINATION.getParameter().getPossibleValues().get(1))) {
            RomanNumeralInterface roman = new LegacyRomanNumeralHelper();
            roman.setValue(currentPhysicalOrder);
            return roman.getNumber();
        } else {
            return (String) ParameterCore.METS_EDITOR_DEFAULT_PAGINATION.getParameter().getDefaultValue();
        }
    }

    private static List<String> getImageNameRegexList() {
        String prefix = ConfigCore.getParameterOrDefaultValue(ParameterCore.IMAGE_PREFIX);

        List<String> regexList = new ArrayList<>();
        regexList.add(prefix + "\\.[Tt][Ii][Ff][Ff]?");
        regexList.add(prefix + "\\.[jJ][pP][eE]?[gG]");
        regexList.add(prefix + "\\.[jJ][pP][2]");
        regexList.add(prefix + "\\.[pP][nN][gG]");
        regexList.add(prefix + "\\.[gG][iI][fF]");
        return regexList;
    }

    private static List<String> getDataRegexList() {
        String prefix = ConfigCore.getParameterOrDefaultValue(ParameterCore.IMAGE_PREFIX);

        List<String> regexList = getImageNameRegexList();
        regexList.add(prefix + "\\.[pP][dD][fF]");
        regexList.add(prefix + "\\.[aA][vV][iI]");
        regexList.add(prefix + "\\.[mM][pP][gG]");
        regexList.add(prefix + "\\.[mM][pP]4");
        regexList.add(prefix + "\\.[mM][pP]3");
        regexList.add(prefix + "\\.[wW][aA][vV]");
        regexList.add(prefix + "\\.[wW][mM][vV]");
        regexList.add(prefix + "\\.[fF][lL][vV]");
        regexList.add(prefix + "\\.[oO][gG][gG]");
        regexList.add(prefix + "\\.docx");
        regexList.add(prefix + "\\.xls");
        regexList.add(prefix + "\\.xlsx");
        regexList.add(prefix + "\\.pptx");
        regexList.add(prefix + "\\.ppt");
        return regexList;
    }

}
