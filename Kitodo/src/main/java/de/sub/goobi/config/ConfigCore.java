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

package de.sub.goobi.config;

import de.sub.goobi.helper.Helper;

import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import org.kitodo.config.ConfigMain;
import org.kitodo.services.ServiceManager;

public class ConfigCore extends ConfigMain {
    private static final Logger logger = LogManager.getLogger(ConfigCore.class);
    private static String imagesPath = null;
    private static ServiceManager serviceManager = new ServiceManager();
    private static final String METADATA_DIRECTORY = "MetadatenVerzeichnis";
    public static final String CONFIG_DIR = "KonfigurationVerzeichnis";

    /**
     * Request selected parameter from configuration.
     *
     * @return Parameter as String
     */
    public static String getParameter(String inParameter) {
        try {
            return getConfig().getString(inParameter);
        } catch (RuntimeException e) {
            logger.error(e);
            return "- keine Konfiguration gefunden -";
        }
    }

    /**
     * Request selected parameter with given default value from configuration.
     *
     * @return Parameter as String
     */
    public static String getParameter(String inParameter, String inDefaultIfNull) {
        try {
            return getConfig().getString(inParameter, inDefaultIfNull);
        } catch (RuntimeException e) {
            return inDefaultIfNull;
        }
    }

    /**
     * Request int-parameter from Configuration with default-value.
     *
     * @return Parameter as Int
     */
    public static int getIntParameter(String inParameter, int inDefault) {
        try {
            return getConfig().getInt(inParameter, inDefault);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * den Pfad für die temporären Images zur Darstellung zurückgeben.
     */
    public static String getTempImagesPath() {
        return "/pages/imagesTemp/";
    }

    /**
     * den absoluten Pfad für die temporären Images zurückgeben.
     */
    public static String getTempImagesPathAsCompleteDirectory() {
        FacesContext context = FacesContext.getCurrentInstance();
        String fileName;
        if (imagesPath != null) {
            fileName = imagesPath;
        } else {
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            fileName = session.getServletContext().getRealPath("/pages") + File.separator;

            /* den Ordner neu anlegen, wenn er nicht existiert */
            try {
                serviceManager.getFileService().createDirectory(URI.create(fileName), "imagesTemp");
            } catch (Exception ioe) {
                logger.error("IO error: " + ioe);
                Helper.setFehlerMeldung(Helper.getTranslation("couldNotCreateImageFolder"), ioe.getMessage());
            }
        }
        return fileName;
    }

    public static void setImagesPath(String path) {
        imagesPath = path;
    }

    /**
     * Request boolean parameter from configuration, default if missing: false.
     *
     * @return Parameter as String
     */
    public static boolean getBooleanParameter(String inParameter) {
        return getBooleanParameter(inParameter, false);
    }

    /**
     * Request boolean parameter from configuration.
     *
     * @return Parameter as String
     */
    public static boolean getBooleanParameter(String inParameter, boolean inDefault) {
        return getConfig().getBoolean(inParameter, inDefault);
    }

    /**
     * Request long parameter from configuration.
     *
     * @return Parameter as Long
     */
    public static long getLongParameter(String inParameter, long inDefault) {
        return getConfig().getLong(inParameter, inDefault);
    }

    /**
     * Request Duration parameter from configuration.
     *
     * @return Parameter as Duration
     */
    public static Duration getDurationParameter(String inParameter, TimeUnit timeUnit, long inDefault) {
        long duration = getLongParameter(inParameter, inDefault);
        return new Duration(TimeUnit.MILLISECONDS.convert(duration, timeUnit));
    }

    /**
     * Request String[]-parameter from Configuration.
     *
     * @return Parameter as String[]
     */
    public static String[] getStringArrayParameter(String inParameter) {
        return getConfig().getStringArray(inParameter);
    }

    /**
     * Get Kitodo data directory.
     *
     * @return String
     */
    public static String getKitodoDataDirectory() {
        return getParameter(METADATA_DIRECTORY);
    }

    /**
     * Get Kitodo config directory.
     *
     * @return String
     */
    public static String getKitodoConfigDirectory() {
        return getParameter(CONFIG_DIR);
    }
}
