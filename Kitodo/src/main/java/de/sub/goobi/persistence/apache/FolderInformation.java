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
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class FolderInformation {

    private int id;
    private String title;
    public static final String metadataPath = ConfigCore.getParameter("MetadatenVerzeichnis");
    public static String DIRECTORY_SUFFIX = ConfigCore.getParameter("DIRECTORY_SUFFIX", "tif");
    public static String DIRECTORY_PREFIX = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");

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
    public URI getImagesTifDirectory(boolean useFallBack) {
        File dir = new File(getImagesDirectory());
        DIRECTORY_SUFFIX = ConfigCore.getParameter("DIRECTORY_SUFFIX", "tif");
        DIRECTORY_PREFIX = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");
        /* nur die _tif-Ordner anzeigen, die nicht mir orig_ anfangen */
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + DIRECTORY_SUFFIX) && !name.startsWith(DIRECTORY_PREFIX + "_"));
            }
        };

        String tifOrdner = "";
        String[] verzeichnisse = fileService.list(filterVerz, dir);

        if (verzeichnisse != null) {
            for (String aVerzeichnisse : verzeichnisse) {
                tifOrdner = aVerzeichnisse;
            }
        }

        if (tifOrdner.equals("") && useFallBack) {
            String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                String[] folderList = fileService.list(dir);
                for (String folder : folderList) {
                    if (folder.endsWith(suffix)) {
                        tifOrdner = folder;
                        break;
                    }
                }
            }
        }
        if (!tifOrdner.equals("") && useFallBack) {
            String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                File tif = new File(tifOrdner);
                String[] files = fileService.list(tif);
                if (files == null || files.length == 0) {
                    String[] folderList = fileService.list(dir);
                    for (String folder : folderList) {
                        if (folder.endsWith(suffix)) {
                            tifOrdner = folder;
                            break;
                        }
                    }
                }
            }
        }

        if (tifOrdner.equals("")) {
            tifOrdner = this.title + "_" + DIRECTORY_SUFFIX;
        }

        String rueckgabe = getImagesDirectory() + tifOrdner;

        if (!rueckgabe.endsWith(File.separator)) {
            rueckgabe += File.separator;
        }

        return URI.create(rueckgabe);
    }

    /**
     * Get tif directory exists.
     *
     * @return true if the Tif-Image-Directory exists, false if not
     */
    public Boolean getTifDirectoryExists() {
        File testMe;

        testMe = new File(getImagesTifDirectory(true));

        return testMe.list() != null && testMe.exists() && fileService.list(testMe).length > 0;
    }

    /**
     * Get images orig directory.
     *
     * @param useFallBack
     *            boolean
     * @return String
     */
    public URI getImagesOrigDirectory(boolean useFallBack) {
        if (ConfigCore.getBooleanParameter("useOrigFolder", true)) {
            File dir = new File(getImagesDirectory());
            DIRECTORY_SUFFIX = ConfigCore.getParameter("DIRECTORY_SUFFIX", "tif");
            DIRECTORY_PREFIX = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");
            /* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
            FilenameFilter filterVerz = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith("_" + DIRECTORY_SUFFIX) && name.startsWith(DIRECTORY_PREFIX + "_"));
                }
            };

            String origOrdner = "";
            String[] verzeichnisse = fileService.list(filterVerz, dir);
            for (int i = 0; i < verzeichnisse.length; i++) {
                origOrdner = verzeichnisse[i];
            }
            if (origOrdner.equals("") && useFallBack) {
                String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    String[] folderList = fileService.list(dir);
                    for (String folder : folderList) {
                        if (folder.endsWith(suffix)) {
                            origOrdner = folder;
                            break;
                        }
                    }
                }
            }
            if (!origOrdner.equals("") && useFallBack) {
                String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    File tif = new File(origOrdner);
                    String[] files = fileService.list(tif);
                    if (files == null || files.length == 0) {
                        String[] folderList = fileService.list(dir);
                        for (String folder : folderList) {
                            if (folder.endsWith(suffix)) {
                                origOrdner = folder;
                                break;
                            }
                        }
                    }
                }
            }

            if (origOrdner.equals("")) {
                origOrdner = DIRECTORY_PREFIX + "_" + this.title + "_" + DIRECTORY_SUFFIX;
            }

            String rueckgabe = getImagesDirectory() + origOrdner + File.separator;

            return URI.create(rueckgabe);
        } else {
            return getImagesTifDirectory(useFallBack);
        }
    }

    /**
     * Get images directory.
     *
     * @return path
     */
    public URI getImagesDirectory() {
        String pfad = getProcessDataDirectory() + "images" + File.separator;

        return URI.create(pfad);
    }

    /**
     * Get process data directory.
     *
     * @return path
     */
    public URI getProcessDataDirectory() {
        String pfad = metadataPath + this.id + File.separator;
        pfad = pfad.replaceAll(" ", "__");
        return URI.create(pfad);
    }

    public URI getOcrDirectory() {
        return URI.create(getProcessDataDirectory() + "ocr" + File.separator);
    }

    public URI getTxtDirectory() {
        return URI.create(getOcrDirectory() + this.title + "_txt" + File.separator);
    }

    public URI getWordDirectory() {
        return URI.create(getOcrDirectory() + this.title + "_wc" + File.separator);
    }

    public URI getPdfDirectory() {
        return URI.create(getOcrDirectory() + this.title + "_pdf" + File.separator);
    }

    public URI getAltoDirectory() {
        return URI.create(getOcrDirectory() + this.title + "_alto" + File.separator);
    }

    public URI getImportDirectory() {
        return URI.create(getProcessDataDirectory() + "import" + File.separator);
    }

    public URI getMetadataFilePath() {
        return URI.create(getProcessDataDirectory() + "meta.xml");
    }

    /**
     * Get source directory.
     *
     * @return path
     */
    public URI getSourceDirectory() {
        File dir = new File(getImagesDirectory());
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + "source"));
            }
        };
        File sourceFolder = null;
        String[] verzeichnisse = fileService.list(filterVerz, dir);
        if (verzeichnisse == null || verzeichnisse.length == 0) {
            sourceFolder = new File(dir, title + "_source");
            if (ConfigCore.getBooleanParameter("createSourceFolder", false)) {
                sourceFolder.mkdir();
            }
        } else {
            sourceFolder = new File(dir, verzeichnisse[0]);
        }

        return URI.create(sourceFolder.getAbsolutePath());
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
        } catch (SecurityException e) {

        } catch (NoSuchMethodException e) {

        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        String folder = this.getImagesTifDirectory(false).toString();
        folder = folder.substring(0, folder.lastIndexOf("_"));
        folder = folder + "_" + methodName;
        if (new File(folder).exists()) {
            return folder;
        }
        return null;
    }

    /**
     * Get data files.
     *
     * @return String
     */
    public List<String> getDataFiles() throws InvalidImagesException {
        File dir;
        try {
            dir = new File(getImagesTifDirectory(true));
        } catch (Exception e) {
            throw new InvalidImagesException(e);
        }
        /* Verzeichnis einlesen */
        String[] dateien = fileService.list(Helper.dataFilter, dir);
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

    public static class GoobiImageFileComparator implements Comparator<String> {

        @Override
        public int compare(String firstString, String secondString) {
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
