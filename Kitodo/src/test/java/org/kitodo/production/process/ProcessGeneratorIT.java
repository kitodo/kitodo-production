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

package org.kitodo.production.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Template;
import org.kitodo.production.workflow.model.Converter;

public class ProcessGeneratorIT {

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGenerateProcess() throws Exception {
        ProcessGenerator processGenerator = new ProcessGenerator();
        boolean generated = processGenerator.generateProcess(4, 1);
        assertTrue("Process was not generated!", generated);

        Process process = processGenerator.getGeneratedProcess();
        Template template = processGenerator.getTemplate();

        Ruleset expectedRuleset = template.getRuleset();
        Ruleset actualRuleset = process.getRuleset();
        assertEquals("Ruleset was copied incorrectly!", expectedRuleset, actualRuleset);

        int expected = template.getTasks().size();
        int actual = process.getTasks().size();
        assertEquals("Task were copied incorrectly!", expected, actual);
    }

    @Test
    //TODO: when error handling will be changed it needs to check for thrown exception
    public void shouldNotGenerateProcess() throws Exception {
        ProcessGenerator processGenerator = new ProcessGenerator();
        boolean generated = processGenerator.generateProcess(2, 1);
        assertFalse("Process was generated!", generated);
    }

    @Test
    public void shouldCopyTasks() throws Exception {
        Converter converter = new Converter("gateway-test1");

        Template template = new Template();
        template.setTitle("Title");
        converter.convertWorkflowToTemplate(template);
        Process process = new Process();

        ProcessGenerator.copyTasks(template, process);
        int actual = process.getTasks().size();
        assertEquals("Task were copied incorrectly!", 5, actual);
    }
}
