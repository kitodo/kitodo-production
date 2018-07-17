package org.kitodo.config.xml.fileformats;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Locale;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.kitodo.api.imagemanagement.ImageFileFormat;

public class FileFormatsConfigIT {
    @Test
    public void testFileFormatsConfig() throws JAXBException, IOException {
        assertThat("kitodo_fileFormats.xml does not contain exactly 8 entries",
            FileFormatsConfig.getFileFormats().size(), is(equalTo(8)));
        FileFormat tiff = FileFormatsConfig.getFileFormat("image/tiff").get();
        assertThat("Wrong label without declared language of TIFF file format", tiff.getLabel(),
            is(equalTo("Tagged Image File Format (image/tiff, *.tif)")));
        assertThat("Wrong label with declared language range of TIFF file format",
            tiff.getLabel(Locale.LanguageRange.parse("fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5")),
            is(equalTo("Tagged Image File Format (image/tiff, *.tif)")));
        assertThat("Long time preservation validation file type is missing for TIFF file format",
            tiff.getFileType().isPresent(), is(true));
        assertThat("Image management file format of TIFF file format is not TIFF", tiff.getImageFileFormat().get(),
            is(equalTo(ImageFileFormat.TIFF)));
    }
}
