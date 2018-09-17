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

package org.kitodo.config;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import org.kitodo.config.enums.Parameter;
import org.kitodo.helper.Helper;
import org.kitodo.services.ServiceManager;

public class ConfigCore extends Config {
    private static final Logger logger = LogManager.getLogger(ConfigCore.class);
    private static URI imagesPath = null;
    private static ServiceManager serviceManager = new ServiceManager();
    public static final String IMAGES_TEMP = "/pages/imagesTemp/";

    /**
     * Private constructor to hide the implicit public one.
     */
    private ConfigCore() {

    }

    /**
     * Return the absolute path for the temporary images directory. Method creates
     * also this folder in case it doesn't exist.
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
            } catch (IOException | RuntimeException e) {
                Helper.setErrorMessage(Helper.getTranslation("couldNotCreateImageFolder"), logger, e);
            }
        }
        return uri;
    }

    static void setImagesPath(URI path) {
        imagesPath = path;
    }

    /**
     * Request long parameter from configuration.
     *
     * @return Parameter as Long
     */
    static long getLongParameter(String inParameter, long inDefault) {
        return getConfig().getLong(inParameter, inDefault);
    }

    /**
     * Request long parameter from configuration.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @param inDefault
     *            default value
     * @return Parameter as Long
     */
    public static long getLongParameter(Parameter key, long inDefault) {
        return getLongParameter(key.getName(), inDefault);
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
     * Request Duration parameter from configuration.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @param timeUnit
     *            as TimeUnit
     * @param defaultValue
     *            default value
     * @return Parameter as Duration
     */
    public static Duration getDurationParameter(Parameter key, TimeUnit timeUnit, long defaultValue) {
        return getDurationParameter(key.getName(), timeUnit, defaultValue);
    }

    /**
     * Request String[]-parameter from Configuration.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return Parameter as String[]
     */
    public static String[] getStringArrayParameter(Parameter key) {
        return getConfig().getStringArray(key.getName());
    }

    /**
     * Get Kitodo diagram directory.
     *
     * @return String
     */
    public static String getKitodoDiagramDirectory() {
        return getParameter(Parameter.DIR_DIAGRAMS);
    }
}
