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

package org.kitodo.production.plugin.opac.pica;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;

/**
 * The class UGHUtils provides utility methods used in the plug-in.
 *
 * @author unascribed
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
class UGHUtils {
    private static final Logger logger = LogManager.getLogger(UGHUtils.class);

    /**
     * The function addMetadatum() adds the meta data element given in terms of
     * type identifier String and value String to the given document structure
     * element, using the given rule set.
     *
     * @param inStruct
     *            structure element to add the meta data element to
     * @param inPrefs
     *            rule set to use
     * @param inMetadataType
     *            type of the meta data element to add
     * @param inValue
     *            value of the meta data element to add
     */
    private static void addMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
        /* wenn kein Wert vorhanden oder das DocStruct null, dann gleich raus */
        if (inValue.equals("") || inStruct == null || inStruct.getType() == null) {
            return;
        }
        /* andernfalls dem DocStruct das passende Metadatum zuweisen */
        MetadataType mdt = inPrefs.getMetadataTypeByName(inMetadataType);
        try {
            Metadata md = new Metadata(mdt);
            md.setType(mdt);
            md.setValue(inValue);
            inStruct.addMetadata(md);
        } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
            logger.error(e);
        }
    }

    /**
     * The function addMetadatum() adds meta data elements of the type whose
     * identifier String is given for all value Strings to the given document
     * structure element, using the given rule set.
     *
     * @param inStruct
     *            structure element to add the meta data elements to
     * @param inPrefs
     *            rule set to use
     * @param inMetadataType
     *            type of the meta data elements to add
     * @param inValues
     *            values of the meta data elements to add
     */
    private static void addMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType,
            Iterable<String> inValues) {
        for (String inValue : inValues) {
            addMetadatum(inStruct, inPrefs, inMetadataType, inValue);
        }
    }

    /**
     * The function replaceMetadatum() removes all meta data elements whose type
     * is equal to the type identified by the given String from a document
     * structure element and adds a new meta data element with the given meta
     * data element given in terms of type identifier String and value String to
     * the given document structure element, using the given rule set.
     *
     * @param inStruct
     *            structure element to replace the meta data elements in
     * @param inPrefs
     *            rule set to use
     * @param inMetadataType
     *            type of the meta data elements to replace
     * @param inValue
     *            value of the meta data element to add
     */
    static void replaceMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
        /* vorhandenes Element löschen */
        MetadataType mdt = inPrefs.getMetadataTypeByName(inMetadataType);
        if (mdt == null) {
            return;
        }
        if (inStruct != null && inStruct.getAllMetadataByType(mdt).size() > 0) {
            List<? extends Metadata> metadataList = inStruct.getAllMetadataByType(mdt);
            for (Metadata metadata : metadataList) {
                inStruct.removeMetadata(metadata);
            }
        }
        /* Element neu hinzufügen */
        addMetadatum(inStruct, inPrefs, inMetadataType, inValue);
    }

    /**
     * The function replaceMetadatum() removes all meta data elements whose type
     * is equal to the type identified by the given String from a document
     * structure element and adds new meta data elements of the type whose
     * identifier String is given for all value Strings to the given document
     * structure element, using the given rule set.
     *
     * @param inStruct
     *            structure element to replace the meta data elements in
     * @param inPrefs
     *            rule set to use
     * @param inMetadataType
     *            type of the meta data elements to replace
     * @param inValues
     *            values of the meta data elements to add
     */
    static void replaceMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, Iterable<String> inValues) {
        /* vorhandenes Element löschen */
        MetadataType mdt = inPrefs.getMetadataTypeByName(inMetadataType);
        if (mdt == null) {
            return;
        }
        if (inStruct != null && inStruct.getAllMetadataByType(mdt).size() > 0) {
            List<? extends Metadata> metadataList = inStruct.getAllMetadataByType(mdt);
            for (Metadata metadata : metadataList) {
                inStruct.removeMetadata(metadata);
            }
        }
        /* Element neu hinzufügen */
        addMetadatum(inStruct, inPrefs, inMetadataType, inValues);
    }

    /**
     * The function convertLanguage() uses one of the mapping files
     * “kitodo_opacLanguages.txt” to replace the passed-in value by a
     * configurable replacement. The mapping file is expected to be a plain text
     * file, encoded as UTF-8, where each line defines a replacement pair as:
     * replacement—white space (U+0020) character—value to be replaced. If no
     * replacement is found, if the value to replace contains white space
     * characters, or if an error occurs (i.e. the mapping file cannot be read),
     * the value passed-in is returned. Which mapping is used depends on the
     * availability of a user context, @see {@link #open(String)}.
     *
     * @param inLanguage
     *            values to replace
     * @return replacements
     */
    // TODO: Create a own class for iso 639 (?) Mappings or move this to UGH
    static String convertLanguage(String inLanguage) {
        /* Datei zeilenweise durchlaufen und die Sprache vergleichen */
        try (BufferedReader in = open(PicaPlugin.LANGUAGES_MAPPING_FILE)) {
            String str;
            while ((str = in.readLine()) != null) {
                if (str.length() > 0 && str.split(" ")[1].equals(inLanguage)) {
                    in.close();
                    return str.split(" ")[0];
                }
            }
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), ""));
            logger.error(e);
        }
        return inLanguage;
    }

    /**
     * The function convertLanguages() uses the function
     * {@link #convertLanguage(String)} to replace the passed-in values by a
     * configurable replacement. If no replacement is found, if the value to
     * replace contains white space characters, or if an error occurs (i.e. the
     * mapping file cannot be read), the value passed-in is returned.
     *
     * @param inLanguages
     *            values to replace
     * @return replacements
     */
    static Iterable<String> convertLanguages(Iterable<String> inLanguages) {
        LinkedList<String> result = new LinkedList<>();
        for (String inLanguage : inLanguages) {
            result.add(convertLanguage(inLanguage));
        }
        return result;
    }

    /**
     * The function open() opens a file. In a user session context, the file is
     * taken from the web application’s deployment directory
     * (…/WEB-INF/classes), if not, it is taken from the CONFIG_DIR specified in
     * the CONFIG_FILE.
     *
     * TODO: Community needs to decide: Is this behaviour really what we want?
     * Shouldn’t it <em>always</em> be the configured directory?
     *
     * @param fileName
     *            File to open
     * @return a BufferedReader for reading the file
     * @throws FileNotFoundException
     *             if the file does not exist, is a directory rather than a
     *             regular file, or for some other reason cannot be opened for
     *             reading
     * @throws UnsupportedEncodingException
     *             If the named charset is not supported
     */
    private static BufferedReader open(String fileName) throws IOException {
        String path = PicaPlugin.getConfigDir();
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            path = FilenameUtils.concat(session.getServletContext().getRealPath("/WEB-INF"), "classes");
        }
        String file = FilenameUtils.concat(path, fileName);
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    }

}
