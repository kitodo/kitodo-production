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

package org.kitodo.production.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class WorkflowConverterIT {

    private static final String MESSAGE = "Workflow was not converted correctly!";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAsObject() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Workflow workflow = workflowConverter.getAsObject(null, null, "2");
        assertEquals(MESSAGE, 2, workflow.getId().intValue());
    }

    @Test
    public void shouldGetAsObjectIncorrectString() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Workflow workflow = workflowConverter.getAsObject(null, null, "in");
        assertEquals(MESSAGE, "0", String.valueOf(workflow));
    }

    @Test
    public void shouldGetAsObjectIncorrectId() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Workflow workflow = workflowConverter.getAsObject(null, null, "10");
        assertEquals(MESSAGE, "0", String.valueOf(workflow.getId()));
    }

    @Test
    public void shouldGetAsObjectNullObject() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Object workflow = workflowConverter.getAsObject(null, null, null);
        assertNull(MESSAGE, workflow);
    }

    @Test
    public void shouldGetAsString() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Workflow newWorkflow = new Workflow();
        newWorkflow.setId(20);
        String workflow = workflowConverter.getAsString(null, null, newWorkflow);
        assertEquals(MESSAGE, "20", workflow);
    }

    @Test
    public void shouldGetAsStringWithoutId() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Workflow newWorkflow = new Workflow();
        String workflow = workflowConverter.getAsString(null, null, newWorkflow);
        assertEquals(MESSAGE, "0", workflow);
    }

    @Test
    public void shouldGetAsStringWithWorkflow() throws DAOException {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Workflow workflow = ServiceManager.getWorkflowService().getById(20);
        String workflowId = workflowConverter.getAsString(null, null, workflow);
        assertEquals(MESSAGE, "20", workflowId);
    }

    @Test
    public void shouldNotGetAsStringNullObject() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        String workflow = workflowConverter.getAsString(null, null, null);
        assertNull(MESSAGE, workflow);
    }
}
