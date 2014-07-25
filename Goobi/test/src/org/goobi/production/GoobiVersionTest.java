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

import org.junit.Test;

import java.util.jar.Manifest;

import static junit.framework.Assert.assertEquals;


public class GoobiVersionTest {

    private static final String VERSION = "1.2.3";
    private static final String BUILDDATE = "17-Februrary-2011";

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfGoobiSectionIsMissingInManifest() {
        GoobiVersion.setupFromManifest(new Manifest());
    }

    @Test
    public void attributeVersionShouldBeEqualToImplementationVersion() {
        Manifest manifest = createManifestWithValues();

        GoobiVersion.setupFromManifest(manifest);

        assertEquals("Version attribute should be equal to Implementation-Version as specified in the given Manifest.",
                VERSION, GoobiVersion.getVersion());
    }

    @Test
    public void attributeBuildversionShouldBeEqualToImplementationVersion() {
        Manifest manifest = createManifestWithValues();

        GoobiVersion.setupFromManifest(manifest);

        assertEquals("Buildversion attribute should be equal to Implementation-Version as specified in the given Manifest.",
                VERSION, GoobiVersion.getBuildversion());
    }

    @Test
    public void attributeBuilddateShouldBeEqualToImplementationBuildDate() {
        Manifest manifest = createManifestWithValues();

        GoobiVersion.setupFromManifest(manifest);

        assertEquals("Builddate attribute should be equal to Implementation-Build-Date as specified in the given Manifest.",
                BUILDDATE, GoobiVersion.getBuilddate());
    }

    private Manifest createManifestWithValues() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Implementation-Version", VERSION);
        manifest.getMainAttributes().putValue("Implementation-Build-Date", BUILDDATE);
        return manifest;
    }

}
