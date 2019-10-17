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

package org.kitodo.dataeditor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.dataformat.DataFormatVersionProvider;

/**
 * Provides methods to get information about module or data format.
 */
public class VersionProvider {
    private static DataFormatVersionProvider dataFormatVersionProvider = new DataFormatVersionProvider();
    private static final Logger logger = LogManager.getLogger(VersionProvider.class);

    /**
     * Private constructor to hide the implicit public one.
     */
    private VersionProvider() {

    }

    /**
     * Reading version info out of the manifest file of current jar.
     *
     * @return The version info as String. (module or application name - version -
     *         build time)
     * @throws IOException
     *             IOException is thrown if an error occurs while reading the
     *             MANIFEST.MF file.
     */
    public static String getModuleVersionInfo() throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
                .getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            URL manifestUrl = resources.nextElement();
            try (InputStream inputStream = manifestUrl.openStream()) {
                Manifest manifest = new Manifest(inputStream);
                Attributes mainAttributes = manifest.getMainAttributes();
                String implementationTitle = mainAttributes.getValue("Implementation-Title");
                if (implementationTitle != null && implementationTitle.equals("Kitodo - Data Editor")) {
                    String implementationVersion = mainAttributes.getValue("Implementation-Version");
                    String buildTime = mainAttributes.getValue("Implementation-Build-Date");
                    return implementationTitle + " - " + implementationVersion + " (" + buildTime + ")";
                }
            }
        }
        logger.error("Could not read application version info for writing in header of mets file!");
        return "Version info is missing";
    }

    /**
     * Gets the current version of used data format.
     * 
     * @return the current version of used data format
     */
    public static String getDataFormatVersion() {
        return dataFormatVersionProvider.getDataFormatVersion();
    }
}
