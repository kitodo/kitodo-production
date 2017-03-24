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

import static junit.framework.Assert.assertEquals;

import java.util.jar.Manifest;

import org.junit.Test;

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

        assertEquals(
                "Buildversion attribute should be equal to Implementation-Version as specified in the given Manifest.",
                VERSION, GoobiVersion.getBuildversion());
    }

    @Test
    public void attributeBuilddateShouldBeEqualToImplementationBuildDate() {
        Manifest manifest = createManifestWithValues();

        GoobiVersion.setupFromManifest(manifest);

        assertEquals(
                "Builddate attribute should be equal to Implementation-Build-Date as specified in the given Manifest.",
                BUILDDATE, GoobiVersion.getBuilddate());
    }

    private Manifest createManifestWithValues() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Implementation-Version", VERSION);
        manifest.getMainAttributes().putValue("Implementation-Build-Date", BUILDDATE);
        return manifest;
    }

}
