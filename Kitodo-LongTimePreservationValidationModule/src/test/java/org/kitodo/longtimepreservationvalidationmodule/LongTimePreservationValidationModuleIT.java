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
import java.util.Arrays;

import org.junit.Test;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.longtimepreservation.FileType;
import org.kitodo.api.validation.longtimepreservation.LongTimePreservationValidationInterface;

public class LongTimePreservationValidationModuleIT {
    private static final URI CORRUPTED_TIF_URI, GIF_URI, JAVA_URI, JP2_URI, JPG_URI, PDF_URI, PNG_URI, TIF_URI;

    static {
        try {
            CORRUPTED_TIF_URI = new URI("src/test/resources/corrupted.tif");
            GIF_URI = new URI("src/test/resources/rose.gif");
            JAVA_URI = new URI(
                    "src/test/java/org/kitodo/longtimepreservationvalidationmodule/LongTimePreservationValidationModuleIT.java");
            JP2_URI = new URI("src/test/resources/rose.jp2");
            JPG_URI = new URI("src/test/resources/rose.jpg");
            PDF_URI = new URI("src/test/resources/rose.pdf");
            PNG_URI = new URI("src/test/resources/rose.png");
            TIF_URI = new URI("src/test/resources/rose.tif");
        } catch (URISyntaxException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Test
    public void testThatACorruptedFileDoesNotValidate() {
        LongTimePreservationValidationInterface validator = new LongTimePreservationValidationModule();
        ValidationResult validationResult = validator.validate(CORRUPTED_TIF_URI, FileType.TIFF);
        assertThat(validationResult.getState(), is(equalTo(State.ERROR)));
        assertThat(validationResult.getResultMessages(),
            is(equalTo(Arrays.asList("IFD offset not word-aligned:  110423"))));
    }

    @Test
    public void testThatFilesOfTheWrongTypeDoNotValidate() {
        LongTimePreservationValidationInterface validator = new LongTimePreservationValidationModule();

        ValidationResult validationResult = validator.validate(JAVA_URI, FileType.PDF);
        assertThat(validationResult.getState(), is(equalTo(State.ERROR)));
        assertThat(validationResult.getResultMessages(), is(equalTo(Arrays.asList("No PDF header", "Offset: 0"))));

        validationResult = validator.validate(PDF_URI, FileType.GIF);
        assertThat(validationResult.getState(), is(equalTo(State.ERROR)));
        assertThat(validationResult.getResultMessages(), is(equalTo(Arrays.asList("Invalid GIF header", "Offset: 0"))));

        validationResult = validator.validate(JP2_URI, FileType.JPEG);
        assertThat(validationResult.getState(), is(equalTo(State.ERROR)));
        assertThat(validationResult.getResultMessages(),
            is(equalTo(Arrays.asList("Invalid JPEG header", "Offset: 0"))));

        validationResult = validator.validate(PNG_URI, FileType.JPEG_2000);
        assertThat(validationResult.getState(), is(equalTo(State.ERROR)));
        assertThat(validationResult.getResultMessages(),
            is(equalTo(Arrays.asList("No JPEG 2000 header", "Offset: 0"))));
    }

    @Test
    public void testThatValidFilesValidateWithDefaultModules() {
        LongTimePreservationValidationInterface validator = new LongTimePreservationValidationModule();
        assertThat(validator.validate(GIF_URI, FileType.GIF).getState(), is(equalTo(State.SUCCESS)));
        assertThat(validator.validate(JP2_URI, FileType.JPEG_2000).getState(), is(equalTo(State.SUCCESS)));
        assertThat(validator.validate(JPG_URI, FileType.JPEG).getState(), is(equalTo(State.SUCCESS)));
        assertThat(validator.validate(PDF_URI, FileType.PDF).getState(), is(equalTo(State.SUCCESS)));
        assertThat(validator.validate(TIF_URI, FileType.TIFF).getState(), is(equalTo(State.SUCCESS)));
    }

    @Test
    public void testThatValidFilesValidateWithExtendedModules() {
        LongTimePreservationValidationInterface validator = new LongTimePreservationValidationModule();
        assertThat(validator.validate(PNG_URI, FileType.PNG).getState(), is(equalTo(State.SUCCESS)));
    }

}
