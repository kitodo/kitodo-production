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

package org.kitodo.longtermpreservationvalidation;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LongTermPreservationValidationInterface;

public class LongTermPreservationValidationIT {
    private static final String NEITHER_WELL_FORMED_NOR_VALID = "Examination result: not well-formed, not valid";
    private static final List<String> WELL_FORMED_AND_VALID = Collections
            .singletonList("Examination result: well-formed, valid");
    private static final URI CORRUPTED_TIF_URI, GIF_URI, JAVA_URI, JP2_URI, JPG_URI, PDF_URI, PNG_URI, TIF_URI;

    static {
        try {
            CORRUPTED_TIF_URI = new URI("src/test/resources/corrupted.tif");
            GIF_URI = new URI("src/test/resources/rose.gif");
            JAVA_URI = new URI(
                    "src/test/java/org/kitodo/longtermpreservationvalidation/LongTermPreservationValidationIT.java");
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
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();
        ValidationResult validationResult = validator.validate(CORRUPTED_TIF_URI, FileType.TIFF);
        assertEquals(State.ERROR, validationResult.getState());
        assertEquals(Arrays.asList(NEITHER_WELL_FORMED_NOR_VALID, "IFD offset not word-aligned:  110423"),
            validationResult.getResultMessages());
    }

    @Test
    public void testThatFilesOfTheWrongTypeDoNotValidate() {
        final String offset = "Offset: 0";
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        ValidationResult validationResult = validator.validate(JAVA_URI, FileType.PDF);
        assertEquals(State.ERROR, validationResult.getState());
        assertEquals(Arrays.asList(NEITHER_WELL_FORMED_NOR_VALID, "No PDF header", offset),
            validationResult.getResultMessages());

        validationResult = validator.validate(PDF_URI, FileType.GIF);
        assertEquals(State.ERROR, validationResult.getState());
        assertEquals(Arrays.asList(NEITHER_WELL_FORMED_NOR_VALID, "Invalid GIF header", offset),
            validationResult.getResultMessages());

        validationResult = validator.validate(JP2_URI, FileType.JPEG);
        assertEquals(State.ERROR, validationResult.getState());
        assertEquals(Arrays.asList(NEITHER_WELL_FORMED_NOR_VALID, "Invalid JPEG header", offset),
            validationResult.getResultMessages());

        validationResult = validator.validate(PNG_URI, FileType.JPEG_2000);
        assertEquals(State.ERROR, validationResult.getState());
        assertEquals(Arrays.asList(NEITHER_WELL_FORMED_NOR_VALID, "No JPEG 2000 header", offset),
            validationResult.getResultMessages());
    }

    @Test
    public void testThatValidFilesValidateWithDefaultModules() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();
        ValidationResult validationResult = validator.validate(GIF_URI, FileType.GIF);
        assertEquals(State.SUCCESS, validationResult.getState());
        assertEquals(WELL_FORMED_AND_VALID, validationResult.getResultMessages());

        validationResult = validator.validate(JP2_URI, FileType.JPEG_2000);
        assertEquals(State.SUCCESS, validationResult.getState());
        assertEquals(WELL_FORMED_AND_VALID, validationResult.getResultMessages());

        validationResult = validator.validate(JPG_URI, FileType.JPEG);
        assertEquals(State.SUCCESS, validationResult.getState());
        assertEquals(WELL_FORMED_AND_VALID, validationResult.getResultMessages());

        validationResult = validator.validate(PDF_URI, FileType.PDF);
        assertEquals(State.SUCCESS, validationResult.getState());
        assertEquals(WELL_FORMED_AND_VALID, validationResult.getResultMessages());

        validationResult = validator.validate(TIF_URI, FileType.TIFF);
        assertEquals(State.SUCCESS, validationResult.getState());
        assertEquals(WELL_FORMED_AND_VALID, validationResult.getResultMessages());
    }

    @Test
    public void testThatValidFilesValidateWithExtendedModules() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();
        ValidationResult validationResult = validator.validate(PNG_URI, FileType.PNG);
        assertEquals(State.SUCCESS, validationResult.getState());
        assertEquals(WELL_FORMED_AND_VALID, validationResult.getResultMessages());
    }

}
