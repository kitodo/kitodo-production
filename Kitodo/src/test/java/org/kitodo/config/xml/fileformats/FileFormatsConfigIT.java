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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale.LanguageRange;

import jakarta.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;
import org.kitodo.api.imagemanagement.ImageFileFormat;


public class FileFormatsConfigIT {
    @Test
    public void testFileFormatsConfig() throws JAXBException {
        assertEquals(8, FileFormatsConfig.getFileFormats().size(), "kitodo_fileFormats.xml does not contain exactly 8 entries");

        FileFormat tiff = FileFormatsConfig.getFileFormat("image/tiff").get();
        assertEquals("Tagged Image File Format (image/tiff, *.tif)", tiff.getLabel(), "Wrong label of TIFF file format");
        assertEquals("Tagged Image File Format (image/tiff, *.tif)", tiff.getLabel(LanguageRange.parse("fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5")), "Wrong label with declared language range of TIFF file format");
        assertTrue(tiff.getFileType().isPresent(), "Long time preservation validation file type is missing for TIFF file format");
        assertEquals(ImageFileFormat.TIFF, tiff.getImageFileFormat().get(), "Image management file format of TIFF file format is not TIFF");
    }

    @Test
    public void testFileFormatsConfigTransliteration() throws JAXBException {
        FileFormat gif = FileFormatsConfig.getFileFormat("image/gif").get();
        assertEquals("Graphics Interchange Format (image/gif, *.gif)", gif.getLabel(), "Wrong label without declared language of GIF file format");
        assertEquals("تنسيق تبادل الرسومات (image/gif, *.gif)", gif.getLabel(LanguageRange.parse("fr;q=0.9,ar;q=0.4,*;q=0.2")), "Wrong label for language requesting arab of GIF file format");
        assertEquals("Graphics Interchange Format (image/gif, *.gif)", gif.getLabel(LanguageRange.parse("en;q=0.9,fr;q=0.4,*;q=0.2")), "Wrong label for language requesting no specialized label of GIF file format");
    }
}
