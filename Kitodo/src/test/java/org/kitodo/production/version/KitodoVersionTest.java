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

package org.kitodo.production.version;

import static org.junit.Assert.assertEquals;

import java.util.jar.Manifest;

import org.junit.Test;

public class KitodoVersionTest {

    private static final String VERSION = "1.2.3";
    private static final String BUILD_DATE = "17-Februrary-2011";

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfKitodoSectionIsMissingInManifest() {
        KitodoVersion.setupFromManifest(new Manifest());
    }

    @Test
    public void attributeVersionShouldBeEqualToImplementationVersion() {
        Manifest manifest = createManifestWithValues();
        KitodoVersion.setupFromManifest(manifest);

        assertEquals("Version attribute should be equal to Implementation-Version as specified in the given Manifest.",
                VERSION, KitodoVersion.getVersion());
    }

    @Test
    public void attributeBuildVersionShouldBeEqualToImplementationVersion() {
        Manifest manifest = createManifestWithValues();
        KitodoVersion.setupFromManifest(manifest);

        assertEquals(
                "BuildVersion attribute should be equal to Implementation-Version as specified in the given Manifest.",
                VERSION, KitodoVersion.getBuildVersion());
    }

    @Test
    public void attributeBuildDateShouldBeEqualToImplementationBuildDate() {
        Manifest manifest = createManifestWithValues();
        KitodoVersion.setupFromManifest(manifest);

        assertEquals(
                "BuildDate attribute should be equal to Implementation-Build-Date as specified in the given Manifest.",
                BUILD_DATE, KitodoVersion.getBuildDate());
    }

    private Manifest createManifestWithValues() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Implementation-Version", VERSION);
        manifest.getMainAttributes().putValue("Implementation-Build-Date", BUILD_DATE);
        return manifest;
    }
}
