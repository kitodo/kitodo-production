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


package org.kitodo.production.services.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Process;

public class ImportServiceTest {

    /**
     * Test that the list of process DTOs is sorted based on a provided projectId.
     * Processes which match the provided projectId should come first.
     */
    @Test
    public void shouldSortProcessesWithProvidedProjectIdFirst()  {

        Project projectOne = new Project();
        projectOne.setId(10);
        Project projectTwo = new Project();
        projectTwo.setId(9);
        Project projectThree = new Project();
        projectThree.setId(8);

        Process processOne = new Process();
        processOne.setProject(projectOne);
        Process processTwo = new Process();
        processTwo.setProject(projectTwo);
        Process processThree = new Process();
        processThree.setProject(projectThree);

        List<Process> processes = new ArrayList<>(Arrays.asList(processOne, processTwo, processThree));

        ImportService importService = new ImportService();
        List<Process> sortedProcesses = importService.sortProcessesByProjectID(processes, 9);

        int projectIdOfFirstProcess = sortedProcesses.getFirst().getProject().getId();

        assertEquals(9, projectIdOfFirstProcess, "Process not sorted based on provided projectId");
    }
}
