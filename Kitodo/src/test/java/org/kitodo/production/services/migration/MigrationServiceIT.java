package org.kitodo.production.services.migration;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

import java.util.ArrayList;
import java.util.List;

public class MigrationServiceIT {

    private MigrationService migrationService = ServiceManager.getMigrationService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void testAddToTemplate() throws DAOException, DataException {
        Template template = ServiceManager.getTemplateService().getById(1);
        Process firstProcess = new Process();
        firstProcess.setTitle("firstMigrationProcess");
        Process secondProcess = new Process();
        secondProcess.setTitle("secondMigrationProcess");

        List<Process> processes = new ArrayList<>();
        processes.add(firstProcess);
        processes.add(secondProcess);

        Assert.assertEquals(4,template.getProcesses().size());

        migrationService.addProcessesToTemplate(template,processes);

        Assert.assertEquals(6,template.getProcesses().size());

        //ServiceManager.getProcessService().getByTitle("firstMigrationProcess");

    }
}
