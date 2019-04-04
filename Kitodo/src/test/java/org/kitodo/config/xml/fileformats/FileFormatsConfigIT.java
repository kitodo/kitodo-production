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

package org.kitodo.config.xml.fileformats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.Locale.LanguageRange;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.kitodo.api.imagemanagement.ImageFileFormat;

public class FileFormatsConfigIT {
    @Test
    public void testFileFormatsConfig() throws JAXBException {
        assertEquals("kitodo_fileFormats.xml does not contain exactly 8 entries", 8,
            FileFormatsConfig.getFileFormats().size());

        FileFormat tiff = FileFormatsConfig.getFileFormat("image/tiff").get();
        assertEquals("Wrong label of TIFF file format", "Tagged Image File Format (image/tiff, *.tif)",
            tiff.getLabel());
        assertEquals("Wrong label with declared language range of TIFF file format",
            "Tagged Image File Format (image/tiff, *.tif)",
            tiff.getLabel(Locale.LanguageRange.parse("fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5")));
        assertTrue("Long time preservation validation file type is missing for TIFF file format",
            tiff.getFileType().isPresent());
        assertEquals("Image management file format of TIFF file format is not TIFF", ImageFileFormat.TIFF,
            tiff.getImageFileFormat().get());
    }

    @Test
    public void testFileFormatsConfigTransliteration() throws JAXBException {
        FileFormat gif = FileFormatsConfig.getFileFormat("image/gif").get();
        assertEquals("Wrong label without declared language of GIF file format",
            "Graphics Interchange Format (image/gif, *.gif)", gif.getLabel());
        assertEquals("Wrong label for language requesting arab of GIF file format",
            "تنسيق تبادل الرسومات (image/gif, *.gif)", gif.getLabel(LanguageRange.parse("fr;q=0.9,ar;q=0.4,*;q=0.2")));
        assertEquals("Wrong label for language requesting no specialized label of GIF file format",
            "Graphics Interchange Format (image/gif, *.gif)",
            gif.getLabel(LanguageRange.parse("en;q=0.9,fr;q=0.4,*;q=0.2")));
    }
}
