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

package de.sub.goobi.persistence.apache;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.InvalidImagesException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.filters.FileNameBeginsAndEndsWithFilter;
import org.kitodo.api.filemanagement.filters.FileNameEndsAndDoesNotBeginWithFilter;
import org.kitodo.api.filemanagement.filters.FileNameEndsWithFilter;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class FolderInformation {

    private int id;
    private String title;
    public static final String metadataPath = ConfigCore.getParameter("MetadatenVerzeichnis");
    public static String DIRECTORY_SUFFIX = ConfigCore.getParameter("DIRECTORY_SUFFIX", "tif");
    public static String DIRECTORY_PREFIX = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");
    private static final Logger logger = LogManager.getLogger(FolderInformation.class);
    private static ServiceManager serviceManager = new ServiceManager();
    private static FileService fileService = serviceManager.getFileService();

    public FolderInformation(int id, String kitodoTitle) {
        this.id = id;
        this.title = kitodoTitle;
    }

    /**
     * Get images tif directory.
     *
     * @param useFallBack
     *            boolean
     * @return String
     */
    public URI getImagesTifDirectory(boolean useFallBack) throws IOException {
        URI dir = getImagesDirectory();
        DIRECTORY_SUFFIX = ConfigCore.getParameter("DIRECTORY_SUFFIX", "tif");
        DIRECTORY_PREFIX = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");
        /* nur die _tif-Ordner anzeigen, die nicht mir orig_ anfangen */
        FilenameFilter filterDirectory = new FileNameEndsAndDoesNotBeginWithFilter(DIRECTORY_PREFIX + "_",
                "_" + DIRECTORY_SUFFIX);
        URI tifOrdner = null;
        ArrayList<URI> directories = fileService.getSubUris(filterDirectory, dir);
        for (URI directory : directories) {
            tifOrdner = directory;
        }

        if (tifOrdner == null && useFallBack) {
            String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                tifOrdner = iterateOverDirectories(dir, suffix);
            }
        }
        if (!(tifOrdner == null) && useFallBack) {
            String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                URI tif = tifOrdner;
                ArrayList<URI> files = fileService.getSubUris(tif);
                if (files == null || files.size() == 0) {
                    ArrayList<URI> folderList = fileService.getSubUris(dir);
                    for (URI folder : folderList) {
                        if (folder.toString().endsWith(suffix)) {
                            tifOrdner = folder;
                            break;
                        }
                    }
                }
            }
        }

        if (tifOrdner == null) {
            tifOrdner = URI.create(this.title + "_" + DIRECTORY_SUFFIX);
        }

        URI rueckgabe = getImagesDirectory().resolve(tifOrdner);

        if (!rueckgabe.toString().endsWith(File.separator)) {
            rueckgabe = rueckgabe.resolve(File.separator);
        }

        return rueckgabe;
    }

    /**
     * Get tif directory exists.
     *
     * @return true if the Tif-Image-Directory exists, false if not
     */
    public Boolean getTifDirectoryExists() {
        try {
            URI testMe = getImagesTifDirectory(true);
            return fileService.getSubUris(testMe) != null && fileService.fileExist(testMe)
                    && fileService.getSubUris(testMe).size() > 0;
        } catch (IOException e) {
            logger.error(e);
            return false;
        }
    }

    /**
     * Get images orig directory.
     *
     * @param useFallBack
     *            boolean
     * @return String
     */
    public URI getImagesOrigDirectory(boolean useFallBack) throws IOException {
        if (ConfigCore.getBooleanParameter("useOrigFolder", true)) {
            URI dir = getImagesDirectory();
            DIRECTORY_SUFFIX = ConfigCore.getParameter("DIRECTORY_SUFFIX", "tif");
            DIRECTORY_PREFIX = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");
            /* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
            FilenameFilter filterDirectory = new FileNameBeginsAndEndsWithFilter(DIRECTORY_PREFIX + "_",
                    "_" + DIRECTORY_SUFFIX);
            URI origOrdner = null;
            ArrayList<URI> verzeichnisse = fileService.getSubUris(filterDirectory, dir);
            // TODO: does it actually make sense?
            for (URI directory : verzeichnisse) {
                origOrdner = directory;
            }
            if (origOrdner == null && useFallBack) {
                String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    origOrdner = iterateOverDirectories(dir, suffix);
                }
            }
            if (!(origOrdner == null) && useFallBack) {
                String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    URI tif = origOrdner;
                    ArrayList<URI> files = fileService.getSubUris(tif);
                    if (files == null || files.size() == 0) {
                        origOrdner = iterateOverDirectories(dir, suffix);
                    }
                }
            }

            if (origOrdner == null) {
                origOrdner = URI.create(DIRECTORY_PREFIX + "_" + this.title + "_" + DIRECTORY_SUFFIX);
            }

            return getImagesDirectory().resolve(origOrdner + File.separator);
        } else {
            return getImagesTifDirectory(useFallBack);
        }
    }

    private URI iterateOverDirectories(URI directory, String suffix) throws IOException {
        ArrayList<URI> folderList = fileService.getSubUris(directory);
        for (URI folder : folderList) {
            if (folder.toString().endsWith(suffix)) {
                return folder;
            }
        }
        return null;
    }

    /**
     * Get images directory.
     *
     * @return path
     */
    public URI getImagesDirectory() {
        return getProcessDataDirectory().resolve("images" + File.separator);
    }

    /**
     * Get process data directory.
     *
     * @return path
     */
    public URI getProcessDataDirectory() {
        String pfad = metadataPath + this.id + File.separator;
        pfad = pfad.replaceAll(" ", "__");
        return new File(pfad).toURI();
    }

    public URI getOcrDirectory() {
        return URI.create(getProcessDataDirectory() + "ocr" + File.separator);
    }

    public URI getMetadataFilePath() {
        return URI.create(getProcessDataDirectory() + "meta.xml");
    }

    /**
     * Get source directory.
     *
     * @return path
     */
    public URI getSourceDirectory() throws IOException {
        URI dir = getImagesDirectory();
        FilenameFilter filterDirectory = new FileNameEndsWithFilter("_source");
        URI sourceFolder;
        ArrayList<URI> directories = fileService.getSubUris(filterDirectory, dir);
        if (directories.size() == 0) {
            sourceFolder = dir.resolve(title + "_source");
            if (ConfigCore.getBooleanParameter("createSourceFolder", false)) {
                fileService.createDirectory(dir, title + "_source");
            }
        } else {
            sourceFolder = dir.resolve(directories.get(0));
        }

        return sourceFolder;
    }

    /**
     * Get method for name.
     *
     * @param methodName
     *            String
     * @return String
     */
    public String getMethodFromName(String methodName) {
        java.lang.reflect.Method method;
        try {
            method = this.getClass().getMethod(methodName);
            Object o = method.invoke(this);
            return (String) o;
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException
                | InvocationTargetException e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * Get data files.
     *
     * @return String
     */
    public List<URI> getDataFiles() throws InvalidImagesException {
        URI dir;
        try {
            dir = getImagesTifDirectory(true);
        } catch (Exception e) {
            throw new InvalidImagesException(e);
        }
        /* Verzeichnis einlesen */
        ArrayList<URI> dataList = new ArrayList<>();
        try {
            ArrayList<URI> files = fileService.getSubUris(Helper.dataFilter, dir);
            if (files.size() > 0) {
                dataList.addAll(files);
                Collections.sort(dataList, new GoobiImageURIComparator());
            }
            return dataList;
        } catch (IOException e) {
            logger.error(e);
            return dataList;
        }
    }

    private static class GoobiImageURIComparator implements Comparator<URI> {

        @Override
        public int compare(URI firstUri, URI secondUri) {
            String firstString = firstUri.toString();
            String secondString = secondUri.toString();
            String imageSorting = ConfigCore.getParameter("ImageSorting", "number");
            firstString = firstString.substring(0, firstString.lastIndexOf("."));
            secondString = secondString.substring(0, secondString.lastIndexOf("."));

            if (imageSorting.equalsIgnoreCase("number")) {
                try {
                    Integer firstInteger = Integer.valueOf(firstString);
                    Integer secondInteger = Integer.valueOf(secondString);
                    return firstInteger.compareTo(secondInteger);
                } catch (NumberFormatException e) {
                    return firstString.compareToIgnoreCase(secondString);
                }
            } else if (imageSorting.equalsIgnoreCase("alphanumeric")) {
                return firstString.compareToIgnoreCase(secondString);
            } else {
                return firstString.compareToIgnoreCase(secondString);
            }
        }

    }
}
