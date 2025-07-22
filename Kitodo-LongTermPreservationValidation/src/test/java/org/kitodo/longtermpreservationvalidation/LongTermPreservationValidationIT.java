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

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LongTermPreservationValidationInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationCondition;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionOperation;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;

public class LongTermPreservationValidationIT {

    private static final URI CORRUPTED_TIF_URI = URI.create("src/test/resources/corrupted.tif");
    private static final URI TIF_URI = URI.create("src/test/resources/rose.tif");
    private static final URI GIF_URI = URI.create("src/test/resources/rose.gif");
    private static final URI JP2_URI = URI.create("src/test/resources/rose.jp2");
    private static final URI JPG_URI = URI.create("src/test/resources/rose.jpg");
    private static final URI PDF_URI = URI.create("src/test/resources/rose.pdf");

    /**
     * Check that valid files are detected as valid and corrupted files are
     * detected as invalid.
     */
    @Test
    public void testSimpleValidationScnearios() {
        assert (simpleValidateFile(CORRUPTED_TIF_URI, FileType.TIFF).equals(LtpValidationResultState.ERROR));
        assert (simpleValidateFile(TIF_URI, FileType.TIFF).equals(LtpValidationResultState.VALID));
        assert (simpleValidateFile(GIF_URI, FileType.GIF).equals(LtpValidationResultState.VALID));
        assert (simpleValidateFile(JP2_URI, FileType.JPEG_2000).equals(LtpValidationResultState.VALID));
        assert (simpleValidateFile(JPG_URI, FileType.JPEG).equals(LtpValidationResultState.VALID));
        assert (simpleValidateFile(PDF_URI, FileType.PDF).equals(LtpValidationResultState.VALID));
    }

    /**
     * Check that valid files are detected as invalid if the wrong file type is
     * specified.
     */
    @Test
    public void testThatFilesOfTheWrongTypeDoNotValidate() {
        assert (simpleValidateFile(TIF_URI, FileType.JPEG).equals(LtpValidationResultState.ERROR));
        assert (simpleValidateFile(GIF_URI, FileType.TIFF).equals(LtpValidationResultState.ERROR));
        assert (simpleValidateFile(JP2_URI, FileType.GIF).equals(LtpValidationResultState.ERROR));
        assert (simpleValidateFile(JPG_URI, FileType.PDF).equals(LtpValidationResultState.ERROR));
        assert (simpleValidateFile(PDF_URI, FileType.JPEG_2000).equals(LtpValidationResultState.ERROR));
    }

    /**
     * Check that a validation condition that declares a valid range is
     * evaluated as valid.
     */
    @Test
    public void testValidInBetweenConditionOperation() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition wideCondition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.IN_BETWEEN, Arrays.asList("60", "80"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationCondition narrowCondition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.IN_BETWEEN, Arrays.asList("70", "70"),
                LtpValidationConditionSeverity.ERROR);

        assert (validator
                .validate(TIF_URI, FileType.TIFF,
                    Arrays.asList(new LtpValidationCondition[] {wideCondition, narrowCondition }))
                .getState().equals(LtpValidationResultState.VALID));
    }

    /**
     * Check that a validation condition that declares an invalid range is
     * evaluated as invalid.
     */
    @Test
    public void testOutsideInBetweenConditionOperation() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition condition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.IN_BETWEEN, Arrays.asList("50", "60"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult result = validator.validate(TIF_URI, FileType.TIFF, Collections.singletonList(condition));

        assert (result.getState().equals(LtpValidationResultState.ERROR));
        assert (!result.getConditionResults().get(0).getPassed());
        assert (result.getConditionResults().get(0).getError().equals(LtpValidationConditionError.CONDITION_FALSE));
    }

    /**
     * Check that a validation condition that declares a range based on a single
     * value (which doesn't make sense) is evaluated as invalid.
     */
    @Test
    public void testInvalidInputInBetweenConditionOperation() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition condition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.IN_BETWEEN, Collections.singletonList("123"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult result = validator.validate(TIF_URI, FileType.TIFF, Collections.singletonList(condition));

        assert (result.getState().equals(LtpValidationResultState.ERROR));
        assert (!result.getConditionResults().get(0).getPassed());
        assert (result.getConditionResults().get(0).getError()
                .equals(LtpValidationConditionError.INCORRECT_NUMBER_OF_CONDITION_VALUES));
    }

    /**
     * Check that a validation condition that declares a range with its first
     * value being larger than the second (which doesn't make sense) is
     * evaluated as invalid.
     */
    @Test
    public void testInverseInputInBetweenConditionOperation() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition condition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.IN_BETWEEN, Arrays.asList("80", "60"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult result = validator.validate(TIF_URI, FileType.TIFF, Collections.singletonList(condition));
        assert (result.getState().equals(LtpValidationResultState.ERROR));
        assert (!result.getConditionResults().get(0).getPassed());
        assert (result.getConditionResults().get(0).getError().equals(LtpValidationConditionError.CONDITION_FALSE));
    }

    /**
     * Test that validation conditions that require a number (in between,
     * smaller than, larger than) will be evaluated as invalid if a string is
     * provided that is not a number.
     */
    @Test
    public void testNotANumberCondition() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition inBetweenCondition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.IN_BETWEEN, Arrays.asList("abc", "def"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationCondition lowerThanCondition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.SMALLER_THAN, Collections.singletonList("abc"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationCondition largerThanCondition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.LARGER_THAN, Collections.singletonList("abc"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult result = validator.validate(TIF_URI, FileType.TIFF,
            Arrays.asList(new LtpValidationCondition[] {inBetweenCondition, lowerThanCondition, largerThanCondition }));

        assert (result.getState().equals(LtpValidationResultState.ERROR));
        for (int i = 0; i < result.getConditionResults().size(); i++) {
            assert (!result.getConditionResults().get(i).getPassed());
            assert (result.getConditionResults().get(i).getError().equals(LtpValidationConditionError.NOT_A_NUMBER));
        }
    }

    /**
     * Check that a validation condition is evaluated as invalid if the image
     * property is not known.
     */
    @Test
    public void testUnknownPropertyCondition() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition condition = new LtpValidationCondition("abc", LtpValidationConditionOperation.EQUAL,
                Collections.singletonList("abc"), LtpValidationConditionSeverity.ERROR);

        LtpValidationResult result = validator.validate(TIF_URI, FileType.TIFF, Collections.singletonList(condition));

        assert (result.getState().equals(LtpValidationResultState.ERROR));
        assert (!result.getConditionResults().get(0).getPassed());
        assert (result.getConditionResults().get(0).getError()
                .equals(LtpValidationConditionError.PROPERTY_DOES_NOT_EXIST));
    }

    /**
     * Check that a validation condition is evaluated as invalid and reported as
     * warning if the severity of the validation condition is declared as
     * warning.
     */
    @Test
    public void testConditionFailureIsReportedAsWarning() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition condition = new LtpValidationCondition("abc", LtpValidationConditionOperation.EQUAL,
                Collections.singletonList("abc"), LtpValidationConditionSeverity.WARNING);

        LtpValidationResult result = validator.validate(TIF_URI, FileType.TIFF, Collections.singletonList(condition));

        assert (result.getState().equals(LtpValidationResultState.WARNING));
        assert (!result.getConditionResults().get(0).getPassed());
        assert (result.getConditionResults().get(0).getError()
                .equals(LtpValidationConditionError.PROPERTY_DOES_NOT_EXIST));
    }

    /**
     * Check that the "one of" validation condition is evaluated as valid if the
     * extracted value is present in the list of potential condition values.
     */
    @Test
    public void testOneOfConditionOperation() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition validCondition = new LtpValidationCondition("colorSpace",
                LtpValidationConditionOperation.ONE_OF, Arrays.asList("cmyk", "rgb"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult validResult = validator.validate(TIF_URI, FileType.TIFF,
            Collections.singletonList(validCondition));
        assert (validResult.getState().equals(LtpValidationResultState.VALID));

        LtpValidationCondition invalidCondition = new LtpValidationCondition("colorSpace",
                LtpValidationConditionOperation.ONE_OF, Arrays.asList("cmyk", "something"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult invalidResult = validator.validate(TIF_URI, FileType.TIFF,
            Collections.singletonList(invalidCondition));
        assert (invalidResult.getState().equals(LtpValidationResultState.ERROR));
        assert (!invalidResult.getConditionResults().get(0).getPassed());
        assert (invalidResult.getConditionResults().get(0).getError()
                .equals(LtpValidationConditionError.CONDITION_FALSE));
    }

    /**
     * Check that the "none of" validation condition is evaluated as valid if
     * the extracted value does not occur amongst the provided condition values.
     */
    @Test
    public void testNoneOfConditionOperation() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition validCondition = new LtpValidationCondition("colorSpace",
                LtpValidationConditionOperation.NONE_OF, Arrays.asList("cmyk", "abc"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult validResult = validator.validate(TIF_URI, FileType.TIFF,
            Collections.singletonList(validCondition));
        assert (validResult.getState().equals(LtpValidationResultState.VALID));

        LtpValidationCondition invalidCondition = new LtpValidationCondition("colorSpace",
                LtpValidationConditionOperation.NONE_OF, Arrays.asList("cmyk", "rgb"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult invalidResult = validator.validate(TIF_URI, FileType.TIFF,
            Collections.singletonList(invalidCondition));
        assert (invalidResult.getState().equals(LtpValidationResultState.ERROR));
        assert (!invalidResult.getConditionResults().get(0).getPassed());
        assert (invalidResult.getConditionResults().get(0).getError()
                .equals(LtpValidationConditionError.CONDITION_FALSE));
    }

    /**
     * Check that the "matches" validation condition is evaluated as valid if
     * the extracted value matches the provided regular expression.
     */
    @Test
    public void testMatchesConditionOperation() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition validCondition = new LtpValidationCondition("filename",
                LtpValidationConditionOperation.MATCHES, Collections.singletonList("rose\\.tif"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult validResult = validator.validate(TIF_URI, FileType.TIFF,
            Collections.singletonList(validCondition));
        assert (validResult.getState().equals(LtpValidationResultState.VALID));

        LtpValidationCondition invalidCondition = new LtpValidationCondition("filename",
                LtpValidationConditionOperation.MATCHES, Collections.singletonList("rose\\.jpg"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult invalidResult = validator.validate(TIF_URI, FileType.TIFF,
            Collections.singletonList(invalidCondition));
        assert (invalidResult.getState().equals(LtpValidationResultState.ERROR));
        assert (!invalidResult.getConditionResults().get(0).getPassed());
        assert (invalidResult.getConditionResults().get(0).getError()
                .equals(LtpValidationConditionError.CONDITION_FALSE));
    }

    /**
     * Checks that the "matches" validation condition detects a syntax error in
     * the regular expression.
     */
    @Test
    public void testMatchesConditionSyntaxError() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition invalidCondition = new LtpValidationCondition("filename",
                LtpValidationConditionOperation.MATCHES, Collections.singletonList("\\p{something}}.tif"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationResult invalidResult = validator.validate(TIF_URI, FileType.TIFF,
            Collections.singletonList(invalidCondition));
        assert (invalidResult.getState().equals(LtpValidationResultState.ERROR));
        assert (invalidResult.getConditionResults().get(0).getError()
                .equals(LtpValidationConditionError.PATTERN_INVALID_SYNTAX));
    }

    /**
     * Check that a validation condition for non-empty values is valid.
     */
    @Test
    public void testValidNonEmptyConditionOperation() {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition wrongValuesCondition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.NON_EMPTY, Arrays.asList("60"),
                LtpValidationConditionSeverity.ERROR);

        LtpValidationCondition validCondition = new LtpValidationCondition("imageWidth",
                LtpValidationConditionOperation.NON_EMPTY, Collections.emptyList(),
                LtpValidationConditionSeverity.ERROR);

        assert (validator
                .validate(TIF_URI, FileType.TIFF,
                    Arrays.asList(new LtpValidationCondition[] { wrongValuesCondition }))
                .getState().equals(LtpValidationResultState.ERROR));

        assert (validator
                .validate(TIF_URI, FileType.TIFF,
                    Arrays.asList(new LtpValidationCondition[] { validCondition }))
                .getState().equals(LtpValidationResultState.VALID));
    }

    private LtpValidationResultState simpleValidateFile(URI file, FileType fileType) {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition condition = new LtpValidationCondition("valid", LtpValidationConditionOperation.EQUAL,
                Collections.singletonList("true"), LtpValidationConditionSeverity.ERROR);

        return validator.validate(file, fileType, Collections.singletonList(condition)).getState();
    }

}
