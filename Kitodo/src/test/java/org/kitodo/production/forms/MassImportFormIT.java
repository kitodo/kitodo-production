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

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldPrepareMassImport() {
        MassImportForm massImportForm = new MassImportForm();
        massImportForm.prepareMassImport(TEMPLATE_ID, PROJECT_ID);
        Assert.assertEquals("Wrong template title", FIRST_TEMPLATE_TITLE, massImportForm.getTemplateTitle());
        AddMetadataDialog addMetadataDialog = massImportForm.getAddMetadataDialog();
        Assert.assertNotNull("'Add metadata' dialog should not be null", addMetadataDialog);
        List<ProcessDetail> metadataTypes = addMetadataDialog.getAllMetadataTypes();
        Assert.assertFalse("List of metadata types should not be empty", metadataTypes.isEmpty());
        ProcessDetail firstDetail = metadataTypes.get(0);
        Assert.assertEquals(String.format("First metadata type should be '%s'", TSL_ATS), TSL_ATS, firstDetail.getLabel());
    }
}
