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

import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class ImageHelper {

    private static final Logger logger = LogManager.getLogger(ImageHelper.class);

    private static final FileService fileService = ServiceManager.getFileService();

    /**
     * Die Images eines Prozesses auf Vollständigkeit prüfen.
     */
    public boolean checkIfImagesValid(String title, URI folder) {
        logger.error("checkIfImagesValid: " + title + " folder: " + folder);
        boolean isValid = true;

        /*
         * alle Bilder durchlaufen und dafür die Seiten anlegen
         */
        if (fileService.fileExist(folder)) {
            List<URI> files = fileService.getSubUris(dataFilter, folder);
            if (files.isEmpty()) {
                Helper.setErrorMessage("[" + title + "] No objects found. "
                        + "Either no objects present, wrong file format or file name does not match your naming "
                        + "convention. (" + ParameterCore.IMAGE_PREFIX + ")");
                return false;
            }

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
