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

package org.kitodo.production.services.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.test.utils.TestConstants;

public class MetadataValidationServiceIT {

    private static final String WRONG_NUMBER_MESSAGE = "Wrong number of result messages";
    private static final String WRONG_STATE_MESSAGE = "Wrong validation state";
    private static final String WRONG_VALIDATION_MESSAGE = "Wrong validation message";
    private static final String SHOULD_NOT_PRODUCE_WARNINGS_MESSAGE = "Lax validation should not produce warnings";
    private static final String SHOULD_SUCCEED_MESSAGE = "Lax validation should succeed without errors";
    private static final String SHOULD_FAIL_MESSAGE = "Lax validation should fail with actual errors";
    private static final String SHOULD_PRODUCE_ERRORS_MESSAGE = "Lax validation should raise error messages for errors";
    private static final String NO_MEDIA_ASSIGNED_MESSAGE = "The structure has no media assigned: Text \"null\"";
    private static final String MISSING_ID_MESSAGE = "Missing value for the identifier.";
    private static final String TEST_FILES_DIR = "./src/test/resources/metadata/metadataFiles/";
    private static final String TEST_META = TEST_FILES_DIR + "testmeta.xml";
    private static final String TEST_KALLIOPE_PARENT =  TEST_FILES_DIR + "testMetadataForKalliopeParentProcess.xml";

    @Test
    public void shouldValidateMetadataByURIAndWarnAboutMissingMedia() {
        ValidationResult result = getValidationResultByURI(TEST_META);
        List<String> validationMessages = new ArrayList<>(result.getResultMessages());
        assertEquals(1, validationMessages.size(), WRONG_NUMBER_MESSAGE);
        assertTrue(validationMessages.contains(NO_MEDIA_ASSIGNED_MESSAGE), WRONG_VALIDATION_MESSAGE);
        assertEquals(State.WARNING, result.getState(), WRONG_STATE_MESSAGE);
    }

    @Test
    public void shouldValidateMetadataByURIAndRaiseError() {
        ValidationResult result = getValidationResultByURI(TEST_KALLIOPE_PARENT);
        assertEquals(State.ERROR, result.getState(), WRONG_STATE_MESSAGE);
    }

    @Test
    public void shouldValidateMetadataByWorkpieceAndWarnAboutMissingMediaAndID() throws IOException, DAOException {
        URI metsUri = Paths.get(TEST_META).toUri();
        RulesetManagementInterface ruleset = ServiceManager.getRulesetManagementService().getRulesetManagement();
        ruleset.load(new File(TestConstants.TEST_RULESET));
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metsUri);
        ValidationResult result = ServiceManager.getMetadataValidationService().validate(workpiece, ruleset);
        List<String> validationMessages = new ArrayList<>(result.getResultMessages());
        // the number of expected warnings is 2 instead of 1 here because validating by workpiece additionally adds a
        // warning for missing process IDs in the workpiece
        assertEquals(2, validationMessages.size(), WRONG_NUMBER_MESSAGE);
        assertTrue(validationMessages.contains(NO_MEDIA_ASSIGNED_MESSAGE), WRONG_VALIDATION_MESSAGE);
        assertTrue(validationMessages.contains(MISSING_ID_MESSAGE), WRONG_VALIDATION_MESSAGE);
        assertEquals(State.WARNING, result.getState(), WRONG_STATE_MESSAGE);
    }

    @Test
    public void shouldValidateMetadataByWorkpieceWithoutWarning() throws IOException, DAOException {
        URI metsUri = Paths.get(TEST_META).toUri();
        RulesetManagementInterface ruleset = ServiceManager.getRulesetManagementService().getRulesetManagement();
        ruleset.load(new File(TestConstants.TEST_RULESET));
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metsUri);
        ValidationResult result = ServiceManager.getMetadataValidationService().validate(workpiece, ruleset, false);
        assertTrue(result.getResultMessages().isEmpty(), SHOULD_NOT_PRODUCE_WARNINGS_MESSAGE);
        assertEquals(State.SUCCESS, result.getState(), SHOULD_SUCCEED_MESSAGE);
    }

    @Test
    public void shouldValidateAndRaiseErrorAndLaxValidation() throws IOException, DAOException {
        URI metsUri = Paths.get(TEST_KALLIOPE_PARENT).toUri();
        RulesetManagementInterface ruleset = ServiceManager.getRulesetManagementService().getRulesetManagement();
        ruleset.load(new File(TestConstants.TEST_RULESET));
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metsUri);
        ValidationResult result = ServiceManager.getMetadataValidationService().validate(workpiece, ruleset, false);
        assertFalse(result.getResultMessages().isEmpty(), SHOULD_PRODUCE_ERRORS_MESSAGE);
        assertEquals(State.ERROR, result.getState(), SHOULD_FAIL_MESSAGE);
    }

    private ValidationResult getValidationResultByURI(String metadataFile) {
        URI metsUri = Paths.get(metadataFile).toUri();
        URI rulesetUri = Paths.get(TestConstants.TEST_RULESET).toUri();
        return ServiceManager.getMetadataValidationService().validate(metsUri, rulesetUri);
    }
}
