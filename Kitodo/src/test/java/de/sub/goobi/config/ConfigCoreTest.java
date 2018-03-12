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

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigCoreTest {

    @Test
    public void shouldSetTempImagesPathAsCompleteDirectory() {
        URI path = URI.create("/pages/newImagesTemp/");
        ConfigCore.setImagesPath(URI.create("/pages/newImagesTemp/"));

        assertEquals("Temp images path was set incorrectly!", path, URI.create("/pages/newImagesTemp/"));
    }

    @Test
    public void shouldGetLongParameter() {
        long actual = ConfigCore.getLongParameter("longParameter", 4L);

        assertEquals("Long value was queried incorrectly!", 2L, actual);
    }

    @Test
    public void shouldGetDurationParameter() {
        Duration actual = ConfigCore.getDurationParameter("longParameter", TimeUnit.SECONDS, 4L);

        assertEquals("Duration value was queried incorrectly!", 2L, actual.getStandardSeconds());
    }

    @Test
    public void shouldGetKitodoConfigDirectory() {
        String expected = "src/test/resources/";

        assertEquals("Directory was queried incorrectly!", expected, ConfigCore.getKitodoConfigDirectory());
    }

    @Test
    public void shouldGetKitodoDataDirectory() {
        String expected = "src/test/resources/";

        assertEquals("Directory was queried incorrectly!", expected, ConfigCore.getKitodoDataDirectory());
    }
}
