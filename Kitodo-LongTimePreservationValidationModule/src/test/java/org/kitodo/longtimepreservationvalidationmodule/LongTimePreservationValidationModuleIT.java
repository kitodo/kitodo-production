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

package org.kitodo.longtimepreservationvalidationmodule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.longtimepreservation.FileType;
import org.kitodo.api.validation.longtimepreservation.LongTimePreservationValidationInterface;

public class LongTimePreservationValidationModuleIT {
    private static final URI GIF_URI = uriOf("src/test/resources/rose.gif");
    private static final URI JAVA_URI = uriOf(
        "src/test/java/org/kitodo/longtimepreservationvalidationmodule/LongTimePreservationValidationModuleIT.java");
    private static final URI JP2_URI = uriOf("src/test/resources/rose.jp2");
    private static final URI JPG_URI = uriOf("src/test/resources/rose.jpg");
    private static final URI PDF_URI = uriOf("src/test/resources/rose.pdf");
    private static final URI PNG_URI = uriOf("src/test/resources/rose.png");
    private static final URI TIF_URI = uriOf("src/test/resources/rose.tif");

    @Test
    public void testValidate() {
        LongTimePreservationValidationInterface validation = new LongTimePreservationValidationModule();

        assertThat(validation.validate(GIF_URI, FileType.GIF).getState(), is(equalTo(State.SUCCESS)));
        assertThat(validation.validate(JAVA_URI, FileType.GIF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JP2_URI, FileType.GIF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JPG_URI, FileType.GIF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PDF_URI, FileType.GIF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PNG_URI, FileType.GIF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(TIF_URI, FileType.GIF).getState(), is(equalTo(State.ERROR)));

        assertThat(validation.validate(GIF_URI, FileType.JPEG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JAVA_URI, FileType.JPEG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JP2_URI, FileType.JPEG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JPG_URI, FileType.JPEG).getState(), is(equalTo(State.SUCCESS)));
        assertThat(validation.validate(PDF_URI, FileType.JPEG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PNG_URI, FileType.JPEG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(TIF_URI, FileType.JPEG).getState(), is(equalTo(State.ERROR)));

        assertThat(validation.validate(GIF_URI, FileType.JPEG_2000).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JAVA_URI, FileType.JPEG_2000).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JP2_URI, FileType.JPEG_2000).getState(), is(equalTo(State.SUCCESS)));
        assertThat(validation.validate(JPG_URI, FileType.JPEG_2000).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PDF_URI, FileType.JPEG_2000).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PNG_URI, FileType.JPEG_2000).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(TIF_URI, FileType.JPEG_2000).getState(), is(equalTo(State.ERROR)));

        assertThat(validation.validate(GIF_URI, FileType.PDF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JAVA_URI, FileType.PDF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JP2_URI, FileType.PDF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JPG_URI, FileType.PDF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PDF_URI, FileType.PDF).getState(), is(equalTo(State.SUCCESS)));
        assertThat(validation.validate(PNG_URI, FileType.PDF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(TIF_URI, FileType.PDF).getState(), is(equalTo(State.ERROR)));

        assertThat(validation.validate(GIF_URI, FileType.PNG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JAVA_URI, FileType.PNG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JP2_URI, FileType.PNG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JPG_URI, FileType.PNG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PDF_URI, FileType.PNG).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PNG_URI, FileType.PNG).getState(), is(equalTo(State.SUCCESS)));
        assertThat(validation.validate(TIF_URI, FileType.PNG).getState(), is(equalTo(State.ERROR)));

        assertThat(validation.validate(GIF_URI, FileType.TIFF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JAVA_URI, FileType.TIFF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JP2_URI, FileType.TIFF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(JPG_URI, FileType.TIFF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PDF_URI, FileType.TIFF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(PNG_URI, FileType.TIFF).getState(), is(equalTo(State.ERROR)));
        assertThat(validation.validate(TIF_URI, FileType.TIFF).getState(), is(equalTo(State.SUCCESS)));
    }

    private static URI uriOf(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

}
