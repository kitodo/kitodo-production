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

package org.kitodo.production.forms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.massimport.AddMetadataDialog;
import org.kitodo.production.forms.massimport.MassImportForm;
import org.kitodo.production.services.ServiceManager;

/**
 * Test for process mass import form.
 */
public class MassImportFormIT {

    private static final int PROJECT_ID = 1;
    private static final int TEMPLATE_ID = 1;
    private static final String FIRST_TEMPLATE_TITLE = "First template";
    private static final String TSL_ATS = "TSL/ATS";

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
    }

    @AfterAll
    public static void cleanup() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldPrepareMassImport() {
        MassImportForm massImportForm = new MassImportForm();
        massImportForm.prepareMassImport(TEMPLATE_ID, PROJECT_ID);
        assertEquals(FIRST_TEMPLATE_TITLE, massImportForm.getTemplateTitle(), "Wrong template title");
        AddMetadataDialog addMetadataDialog = massImportForm.getAddMetadataDialog();
        assertNotNull(addMetadataDialog, "'Add metadata' dialog should not be null");
        List<ProcessDetail> metadataTypes = addMetadataDialog.getAllMetadataTypes();
        assertFalse(metadataTypes.isEmpty(), "List of metadata types should not be empty");
        ProcessDetail firstDetail = metadataTypes.getFirst();
        assertEquals(TSL_ATS, firstDetail.getLabel(), String.format("First metadata type should be '%s'", TSL_ATS));
    }
}
