/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class GoobiVersion {

    private static String version = "N/A";
    private static String buildversion = "N/A";
    private static String builddate = "N/A";

    public static void setupFromManifest(Manifest manifest) throws IllegalArgumentException {
        Attributes mainAttributes = manifest.getMainAttributes();

        version = getValueOrThrowException(mainAttributes, "Implementation-Version");
        buildversion = version;
        builddate = getValueOrThrowException(mainAttributes, "Implementation-Build-Date");
    }

    private static String getValueOrThrowException(Attributes attributes, String attributeName) throws IllegalArgumentException {
        String result = attributes.getValue(attributeName);
        if (null == result) {
            throw new IllegalArgumentException("Manifest does not contain " + attributeName + ". The build may be corrupted.");
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
