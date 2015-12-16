/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
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
