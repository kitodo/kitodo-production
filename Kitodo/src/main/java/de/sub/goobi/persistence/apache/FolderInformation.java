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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;

public class FolderInformation {

    private int id;
    private String title;
    public static final String metadataPath = ConfigCore.getParameter("MetadatenVerzeichnis");
    public static String DIRECTORY_SUFFIX = ConfigCore.getParameter("DIRECTORY_SUFFIX", "tif");
    public static String DIRECTORY_PREFIX = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");

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
    public String getImagesTifDirectory(boolean useFallBack) {
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
        String[] verzeichnisse = dir.list(filterVerz);

        if (verzeichnisse != null) {
            for (int i = 0; i < verzeichnisse.length; i++) {
                tifOrdner = verzeichnisse[i];
            }
        }

        if (tifOrdner.equals("") && useFallBack) {
            String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                String[] folderList = dir.list();
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
                String[] files = tif.list();
                if (files == null || files.length == 0) {
                    String[] folderList = dir.list();
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

        return rueckgabe;
    }

    /**
     * Get tif directory exists.
     *
     * @return true if the Tif-Image-Directory exists, false if not
     */
    public Boolean getTifDirectoryExists() {
        File testMe;

        testMe = new File(getImagesTifDirectory(true));

        if (testMe.list() == null) {
            return false;
        }
        if (testMe.exists() && testMe.list().length > 0) {
            return true;
        } else {
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
    public String getImagesOrigDirectory(boolean useFallBack) {
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
            String[] verzeichnisse = dir.list(filterVerz);
            for (int i = 0; i < verzeichnisse.length; i++) {
                origOrdner = verzeichnisse[i];
            }

            if (origOrdner.equals("") && useFallBack) {
                String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    String[] folderList = dir.list();
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
                    String[] files = tif.list();
                    if (files == null || files.length == 0) {
                        String[] folderList = dir.list();
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

            return rueckgabe;
        } else {
            return getImagesTifDirectory(useFallBack);
        }
    }

    /**
     * Get images directory.
     *
     * @return path
     */
    public String getImagesDirectory() {
        String pfad = getProcessDataDirectory() + "images" + File.separator;

        return pfad;
    }

    /**
     * Get process data directory.
     *
     * @return path
     */
    public String getProcessDataDirectory() {
        String pfad = metadataPath + this.id + File.separator;
        pfad = pfad.replaceAll(" ", "__");
        return pfad;
    }

    public String getOcrDirectory() {
        return getProcessDataDirectory() + "ocr" + File.separator;
    }

    public String getTxtDirectory() {
        return getOcrDirectory() + this.title + "_txt" + File.separator;
    }

    public String getWordDirectory() {
        return getOcrDirectory() + this.title + "_wc" + File.separator;
    }

    public String getPdfDirectory() {
        return getOcrDirectory() + this.title + "_pdf" + File.separator;
    }

    public String getAltoDirectory() {
        return getOcrDirectory() + this.title + "_alto" + File.separator;
    }

    public String getImportDirectory() {
        return getProcessDataDirectory() + "import" + File.separator;
    }

    public String getMetadataFilePath() {
        return getProcessDataDirectory() + "meta.xml";
    }

    /**
     * Get source directory.
     *
     * @return path
     */
    public String getSourceDirectory() {
        File dir = new File(getImagesDirectory());
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + "source"));
            }
        };
        File sourceFolder = null;
        String[] verzeichnisse = dir.list(filterVerz);
        if (verzeichnisse == null || verzeichnisse.length == 0) {
            sourceFolder = new File(dir, title + "_source");
            if (ConfigCore.getBooleanParameter("createSourceFolder", false)) {
                sourceFolder.mkdir();
            }
        } else {
            sourceFolder = new File(dir, verzeichnisse[0]);
        }

        return sourceFolder.getAbsolutePath();
    }

    /**
     * Get folder for process.
     *
     * @param useFallBack
     *            boolean
     * @return Map of paths
     */
    public Map<String, String> getFolderForProcess(boolean useFallBack) {
        Map<String, String> answer = new HashMap<String, String>();
        String processpath = getProcessDataDirectory().replace("\\", "/");
        String tifpath = getImagesTifDirectory(useFallBack).replace("\\", "/");
        String imagepath = getImagesDirectory().replace("\\", "/");
        String origpath = getImagesOrigDirectory(useFallBack).replace("\\", "/");
        String metaFile = getMetadataFilePath().replace("\\", "/");
        String ocrBasisPath = getOcrDirectory().replace("\\", "/");
        String ocrPlaintextPath = getTxtDirectory().replace("\\", "/");
        String sourcepath = getSourceDirectory().replace("\\", "/");
        String importpath = getImportDirectory().replace("\\", "/");
        if (tifpath.endsWith(File.separator)) {
            tifpath = tifpath.substring(0, tifpath.length() - File.separator.length()).replace("\\", "/");
        }
        if (imagepath.endsWith(File.separator)) {
            imagepath = imagepath.substring(0, imagepath.length() - File.separator.length()).replace("\\", "/");
        }
        if (origpath.endsWith(File.separator)) {
            origpath = origpath.substring(0, origpath.length() - File.separator.length()).replace("\\", "/");
        }
        if (processpath.endsWith(File.separator)) {
            processpath = processpath.substring(0, processpath.length() - File.separator.length()).replace("\\", "/");
        }
        if (sourcepath.endsWith(File.separator)) {
            sourcepath = sourcepath.substring(0, sourcepath.length() - File.separator.length()).replace("\\", "/");
        }
        if (ocrBasisPath.endsWith(File.separator)) {
            ocrBasisPath = ocrBasisPath.substring(0, ocrBasisPath.length() - File.separator.length()).replace("\\",
                    "/");
        }
        if (ocrPlaintextPath.endsWith(File.separator)) {
            ocrPlaintextPath = ocrPlaintextPath.substring(0, ocrPlaintextPath.length() - File.separator.length())
                    .replace("\\", "/");
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            answer.put("(tifurl)", "file:/" + tifpath);
        } else {
            answer.put("(tifurl)", "file://" + tifpath);
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            answer.put("(origurl)", "file:/" + origpath);
        } else {
            answer.put("(origurl)", "file://" + origpath);
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            answer.put("(imageurl)", "file:/" + imagepath);
        } else {
            answer.put("(imageurl)", "file://" + imagepath);
        }
        answer.put("(tifpath)", tifpath);
        answer.put("(origpath)", origpath);
        answer.put("(imagepath)", imagepath);
        answer.put("(processpath)", processpath);
        answer.put("(sourcepath)", sourcepath);
        answer.put("(importpath)", importpath);
        answer.put("(ocrbasispath)", ocrBasisPath);
        answer.put("(ocrplaintextpath)", ocrPlaintextPath);
        answer.put("(metaFile)", metaFile);
        return answer;
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
        String folder = this.getImagesTifDirectory(false);
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
