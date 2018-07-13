package org.kitodo.config.xml.fileformats;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.Locale;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.kitodo.api.imagemanagement.ImageFileFormat;

public class FileFormatsConfigIT {
    @Test
    public void testRead() throws JAXBException {
        FileFormatsConfig formats = FileFormatsConfig.read(new File("src/test/resources/fileFormatsConfigTest.xml"));
        assertThat(formats.getFileFormats().size(), is(equalTo(8)));
        FileFormat tiff = formats.getFileFormat("image/tiff").get();
        assertThat(tiff.getLabel(), is(equalTo("Tagged Image File Format (image/tiff, *.tif)")));
        assertThat(tiff.getLabel(Locale.LanguageRange.parse("fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5")),
            is(equalTo("Tagged Image File Format (image/tiff, *.tif)")));
        assertThat(tiff.getFileType().isPresent(), is(true));
        assertThat(tiff.getImageFileFormat().get(), is(equalTo(ImageFileFormat.TIFF)));
    }
}
