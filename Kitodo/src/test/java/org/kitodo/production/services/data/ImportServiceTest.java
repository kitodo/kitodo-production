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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.ProjectDTO;

public class ImportServiceTest {

    /**
     * Test that the list of process DTOs is sorted based on a provided projectId.
     * Processes which match the provided projectId should come first.
     */
    @Test
    public void shouldSortProcessesWithProvidedProjectIdFirst()  {

        ProjectDTO projectOne = new ProjectDTO();
        projectOne.setId(10);
        ProjectDTO projectTwo = new ProjectDTO();
        projectTwo.setId(9);
        ProjectDTO projectThree = new ProjectDTO();
        projectThree.setId(8);

        ProcessDTO processOne = new ProcessDTO();
        processOne.setProject(projectOne);
        ProcessDTO processTwo = new ProcessDTO();
        processTwo.setProject(projectTwo);
        ProcessDTO processThree = new ProcessDTO();
        processThree.setProject(projectThree);

        List<ProcessDTO> processes = new ArrayList<>(Arrays.asList(processOne, processTwo, processThree));

        ImportService importService = new ImportService();
        importService.sortProcessesByProjectID(processes, 9);

        int projectIdOfFirstProcess = processes.get(0).getProject().getId();

        Assert.assertEquals("Process not sorted based on provided projectId",9,
                projectIdOfFirstProcess);
    }
}
