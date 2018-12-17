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
import org.kitodo.config.beans.Parameter;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.exceptions.ConfigParameterException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

public class ConfigCore extends KitodoConfig {
    private static final Logger logger = LogManager.getLogger(ConfigCore.class);
    private static URI imagesPath = null;

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
                uri = ServiceManager.getFileService().createDirectory(Paths.get(fileName).toUri(), "imagesTemp");
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
     * Request string parameter from configuration, if parameter is not there - use
     * default value.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return parameter as String or default value for this parameter
     */
    public static String getParameterOrDefaultValue(ParameterCore key) {
        Parameter parameter = key.getParameter();

        if (parameter.getDefaultValue() instanceof String) {
            return getParameter(key.getName(), (String) parameter.getDefaultValue());
        }
        throw new ConfigParameterException(parameter.getKey(), "String");
    }

    /**
     * Request boolean parameter from configuration, if parameter is not there - use
     * default value.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return parameter as boolean or default value for this parameter
     */
    public static boolean getBooleanParameterOrDefaultValue(ParameterCore key) {
        Parameter parameter = key.getParameter();

        if (parameter.getDefaultValue() instanceof Boolean) {
            return getBooleanParameter(key, (boolean) parameter.getDefaultValue());
        }
        throw new ConfigParameterException(parameter.getKey(), "boolean");
    }

    /**
     * Request int parameter from configuration, if parameter is not there - use
     * default value.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return parameter as int or default value for this parameter
     */
    public static int getIntParameterOrDefaultValue(ParameterCore key) {
        Parameter parameter = key.getParameter();

        if (parameter.getDefaultValue() instanceof Integer) {
            return getIntParameter(key, (int) parameter.getDefaultValue());
        }
        throw new ConfigParameterException(parameter.getKey(), "int");
    }

    /**
     * Request long parameter from configuration, if parameter is not there - use
     * default value.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return Parameter as long or default value
     */
    public static long getLongParameterOrDefaultValue(ParameterCore key) {
        Parameter parameter = key.getParameter();

        if (parameter.getDefaultValue() instanceof Long) {
            return getLongParameter(key, (long) parameter.getDefaultValue());
        }
        throw new ConfigParameterException(parameter.getKey(), "long");
    }

    /**
     * Request long parameter or default value from configuration.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @param defaultValue
     *            as long
     * @return Parameter as long or default value
     */
    public static long getLongParameter(ParameterCore key, long defaultValue) {
        return getConfig().getLong(key.getName(), defaultValue);
    }

    /**
     * Request Duration parameter from configuration.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @param timeUnit
     *            as TimeUnit
     * @return Parameter as Duration
     */
    public static Duration getDurationParameter(ParameterCore key, TimeUnit timeUnit) {
        long duration = getLongParameterOrDefaultValue(key);
        return new Duration(TimeUnit.MILLISECONDS.convert(duration, timeUnit));
    }

    /**
     * Request String[]-parameter from Configuration.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return Parameter as String[]
     */
    public static String[] getStringArrayParameter(ParameterCore key) {
        return getConfig().getStringArray(key.getName());
    }

    /**
     * Get Kitodo diagram directory.
     *
     * @return String
     */
    public static String getKitodoDiagramDirectory() {
        return getParameter(ParameterCore.DIR_DIAGRAMS);
    }
}
