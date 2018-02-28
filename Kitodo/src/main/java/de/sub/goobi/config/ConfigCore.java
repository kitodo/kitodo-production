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
import java.nio.file.Paths;
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
    private static URI imagesPath = null;
    private static ServiceManager serviceManager = new ServiceManager();
    private static final String METADATA_DIRECTORY = "MetadatenVerzeichnis";
    private static final String CONFIG_DIR = "KonfigurationVerzeichnis";
    public static final String IMAGES_TEMP = "/pages/imagesTemp/";

    /**
     * Return the absolute path for the temporary images directory. Method
     * creates also this folder in case it doesn't exist.
     * 
     * @return the path for the temporary images directory as URI
     */
    public static URI getTempImagesPathAsCompleteDirectory() {
        FacesContext context = FacesContext.getCurrentInstance();
        String fileName;
        URI uri = null;
        if (imagesPath != null) {
            uri = imagesPath;
        } else {
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            fileName = session.getServletContext().getRealPath("/pages") + File.separator;
            try {
                uri = serviceManager.getFileService().createDirectory(Paths.get(fileName).toUri(), "imagesTemp");
            } catch (Exception ioe) {
                logger.error("IO error: " + ioe);
                Helper.setFehlerMeldung(Helper.getTranslation("couldNotCreateImageFolder"), ioe.getMessage());
            }
        }
        return uri;
    }

    public static void setImagesPath(URI path) {
        imagesPath = path;
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
