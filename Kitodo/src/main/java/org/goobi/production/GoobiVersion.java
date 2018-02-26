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

package org.goobi.production;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class GoobiVersion {

    private static String version = "N/A";
    private static String buildversion = "N/A";
    private static String builddate = "N/A";

    /**
     * Setup GoobiVersion form manifest.
     *
     * @param manifest as Manifest
     */
    public static void setupFromManifest(Manifest manifest) {
        Attributes mainAttributes = manifest.getMainAttributes();

        version = getValueOrThrowException(mainAttributes, "Implementation-Version");
        buildversion = version;
        builddate = getValueOrThrowException(mainAttributes, "Implementation-Build-Date");
    }

    private static String getValueOrThrowException(Attributes attributes, String attributeName) {
        String result = attributes.getValue(attributeName);
        if (null == result) {
            throw new IllegalArgumentException(
                    "Manifest does not contain " + attributeName + ". The build may be corrupted.");
        }
        return result;
    }

    public static String getVersion() {
        return version;
    }

    public static String getBuildversion() {
        return buildversion;
    }

    public static String getBuilddate() {
        return builddate;
    }
}
