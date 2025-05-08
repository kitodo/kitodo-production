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
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LongTermPreservationValidationInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationCondition;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionOperation;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;


public class LongTermPreservationValidationIT {

    private static final URI CORRUPTED_TIF_URI = URI.create("src/test/resources/corrupted.tif");
    private static final URI TIF_URI = URI.create("src/test/resources/rose.tif");
    private static final URI GIF_URI = URI.create("src/test/resources/rose.gif");
    private static final URI JP2_URI = URI.create("src/test/resources/rose.jp2");
    private static final URI JPG_URI = URI.create("src/test/resources/rose.jpg");
    private static final URI PDF_URI = URI.create("src/test/resources/rose.pdf");

    @Test
    public void testSimpleValidationScnearios() {        
        assert(simpleValidateFile(CORRUPTED_TIF_URI, FileType.TIFF).equals(LtpValidationResultState.ERROR));
        assert(simpleValidateFile(TIF_URI, FileType.TIFF).equals(LtpValidationResultState.VALID));
        assert(simpleValidateFile(GIF_URI, FileType.GIF).equals(LtpValidationResultState.VALID));
        assert(simpleValidateFile(JP2_URI, FileType.JPEG_2000).equals(LtpValidationResultState.VALID));
        assert(simpleValidateFile(JPG_URI, FileType.JPEG).equals(LtpValidationResultState.VALID));
        assert(simpleValidateFile(PDF_URI, FileType.PDF).equals(LtpValidationResultState.VALID));
    }

    @Test
    public void testThatFilesOfTheWrongTypeDoNotValidate() {
        assert(simpleValidateFile(TIF_URI, FileType.JPEG).equals(LtpValidationResultState.ERROR));
        assert(simpleValidateFile(GIF_URI, FileType.TIFF).equals(LtpValidationResultState.ERROR));
        assert(simpleValidateFile(JP2_URI, FileType.GIF).equals(LtpValidationResultState.ERROR));
        assert(simpleValidateFile(JPG_URI, FileType.PDF).equals(LtpValidationResultState.ERROR));
        assert(simpleValidateFile(PDF_URI, FileType.JPEG_2000).equals(LtpValidationResultState.ERROR));
    }

    private LtpValidationResultState simpleValidateFile(URI file, FileType fileType) {
        LongTermPreservationValidationInterface validator = new LongTermPreservationValidation();

        LtpValidationCondition condition = new LtpValidationCondition(
            "valid",
            LtpValidationConditionOperation.EQUAL,
            Collections.singletonList("true"),
            LtpValidationConditionSeverity.ERROR
        );

        return validator.validate(file, fileType, Collections.singletonList(condition)).getState();
    }

}
