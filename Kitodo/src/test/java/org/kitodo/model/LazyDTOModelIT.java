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

package org.kitodo.model;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.dto.WorkflowDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.WorkflowService;
import org.primefaces.model.SortOrder;

public class LazyDTOModelIT {

    private static final ServiceManager serviceManager = new ServiceManager();
    private static WorkflowService workflowService = serviceManager.getWorkflowService();
    private static LazyDTOModel lazyDTOModel = null;

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertWorkflows();
        lazyDTOModel = new LazyDTOModel(workflowService);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetRowData() throws Exception {
        List dockets = workflowService.findAll();
        WorkflowDTO firstWorkflow = (WorkflowDTO) dockets.get(0);
        WorkflowDTO lazyWorkflow = (WorkflowDTO) lazyDTOModel.getRowData(String.valueOf(firstWorkflow.getId()));
        Assert.assertEquals(firstWorkflow.getTitle(), lazyWorkflow.getTitle());
    }

    @Test
    public void shouldLoad() {
        List workflows = lazyDTOModel.load(0, 2, "title", SortOrder.ASCENDING, null);
        WorkflowDTO workflow = (WorkflowDTO) workflows.get(0);
        Assert.assertEquals("gateway", workflow.getTitle());
    }
}
