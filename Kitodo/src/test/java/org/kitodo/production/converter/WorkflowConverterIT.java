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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Workflow;

public class WorkflowConverterIT {

    private static final String MESSAGE = "Workflow was not converted correctly!";

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAsObject() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Workflow workflow = (Workflow) workflowConverter.getAsObject(null, null, "2");
        assertEquals(2, workflow.getId().intValue(), MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectString() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        String workflow = (String) workflowConverter.getAsObject(null, null, "in");
        assertEquals("0", workflow, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectId() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        String workflow = (String) workflowConverter.getAsObject(null, null, "10");
        assertEquals("0", workflow, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectNullObject() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Object workflow = workflowConverter.getAsObject(null, null, null);
        assertNull(workflow, MESSAGE);
    }

    @Test
    public void shouldGetAsString() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Workflow newWorkflow = new Workflow();
        newWorkflow.setId(20);
        String workflow = workflowConverter.getAsString(null, null, newWorkflow);
        assertEquals("20", workflow, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithoutId() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        Workflow newWorkflow = new Workflow();
        String workflow = workflowConverter.getAsString(null, null, newWorkflow);
        assertEquals("0", workflow, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithString() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        String workflow = workflowConverter.getAsString(null, null, "20");
        assertEquals("20", workflow, MESSAGE);
    }

    @Test
    public void shouldNotGetAsStringNullObject() {
        WorkflowConverter workflowConverter = new WorkflowConverter();
        String workflow = workflowConverter.getAsString(null, null, null);
        assertNull(workflow, MESSAGE);
    }
}
