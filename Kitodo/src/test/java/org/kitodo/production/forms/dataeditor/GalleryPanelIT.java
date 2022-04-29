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

package org.kitodo.production.forms.dataeditor;

import static org.awaitility.Awaitility.await;

import java.util.Objects;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.mockito.Mockito;


public class GalleryPanelIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        Client client = new Client();
        ServiceManager.getClientService().saveToDatabase(client);
        User user = new User();
        ServiceManager.getUserService().saveToDatabase(user);
        SecurityTestUtils.addUserDataToSecurityContext(user, client.getId());
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(user, client.getId());
            return Objects.nonNull(ServiceManager.getUserService().getAuthenticatedUser());
        });
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void preventNPEInShowMethodOnEmptyFolder() throws DAOException {
        Integer processId = 2;

        DataEditorForm dummyDataEditorForm = Mockito.spy(DataEditorForm.class);
        // mock checkForChanges call to prevent execution of JavaScript
        Mockito.doNothing().when(dummyDataEditorForm).checkForChanges();
        Process process = ServiceManager.getProcessService().getById(processId);
        dummyDataEditorForm.setProcess(process);
        dummyDataEditorForm.open(processId.toString(), "");
        GalleryPanel galleryPanel = new GalleryPanel(dummyDataEditorForm);

        // excecute critical show method
        galleryPanel.show();

        // asserts
        String expectedDefaultMimeType = "application/octet-stream";
        Assert.assertEquals(expectedDefaultMimeType, galleryPanel.getMediaViewMimeType());
        Assert.assertEquals(expectedDefaultMimeType, galleryPanel.getPreviewMimeType());
    }

    @Test
    public void afterExecutionShowMethodMimetypesAreSetCorrect() throws DAOException, DataException {
        Integer processId = 2;

        DataEditorForm dummyDataEditorForm = Mockito.spy(DataEditorForm.class);
        // mock checkForChanges call to prevent execution of JavaScript
        Mockito.doNothing().when(dummyDataEditorForm).checkForChanges();
        Process process = ServiceManager.getProcessService().getById(processId);
        dummyDataEditorForm.setProcess(process);

        // hard coded folders from MockDatabase class
        process.getProject().setMediaView(process.getProject().getFolders().get(2));
        process.getProject().setPreview(process.getProject().getFolders().get(3));
        // save changed project to database
        ServiceManager.getProjectService().save(process.getProject());
        dummyDataEditorForm.open(processId.toString(), "");
        GalleryPanel galleryPanel = new GalleryPanel(dummyDataEditorForm);

        // execute critical show method
        galleryPanel.show();

        // asserts
        Assert.assertEquals(process.getProject().getMediaView().getMimeType(), galleryPanel.getMediaViewMimeType());
        Assert.assertEquals(process.getProject().getPreview().getMimeType(), galleryPanel.getPreviewMimeType());
    }
}
