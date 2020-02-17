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
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Template;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.model.Converter;

public class ProcessGeneratorIT {

    private static final String NEW = "new";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

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
        boolean generated = processGenerator.generateProcess(5, 1);
        assertTrue("Process was not generated!", generated);

        Process process = processGenerator.getGeneratedProcess();
        Template template = processGenerator.getTemplate();

        Ruleset expectedRuleset = template.getRuleset();
        Ruleset actualRuleset = process.getRuleset();
        assertEquals("Ruleset was copied incorrectly!", expectedRuleset, actualRuleset);

        int expected = template.getTasks().size();
        int actual = process.getTasks().size();
        assertEquals("Tasks were copied incorrectly!", expected, actual);
    }

    @Test
    public void shouldNotGenerateProcess() throws Exception {
        ProcessGenerator processGenerator = new ProcessGenerator();

        expectedEx.expect(ProcessGenerationException.class);
        expectedEx.expectMessage("No steps of the workflow defined.");
        processGenerator.generateProcess(2, 1);
    }

    @Test
    public void shouldAddPropertyForProcess() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);

        ProcessGenerator.addPropertyForProcess(process, NEW, "process");
        assertTrue("Property was added incorrectly!",
            process.getProperties().stream().anyMatch(property -> property.getTitle().equals("new")));
    }

    @Test
    public void shouldAddPropertyForTemplate() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);

        ProcessGenerator.addPropertyForTemplate(process, NEW, "template");
        assertTrue("Property was added incorrectly!",
            process.getTemplates().stream().anyMatch(property -> property.getTitle().equals(NEW)));
    }

    @Test
    public void shouldAddPropertyForWorkpiece() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);

        ProcessGenerator.addPropertyForWorkpiece(process, NEW, "workpiece");
        assertTrue("Property was added incorrectly!",
            process.getWorkpieces().stream().anyMatch(property -> property.getTitle().equals(NEW)));
    }

    @Test
    public void shouldCopyPropertyForProcess() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle(NEW);
        property.setValue("process");

        ProcessGenerator.copyPropertyForProcess(process, new Property());
        assertTrue("Property was copied incorrectly!", process.getProperties().contains(property));
    }

    @Test
    public void shouldCopyPropertyForTemplate() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle(NEW);
        property.setValue("template");

        ProcessGenerator.copyPropertyForTemplate(process, new Property());
        assertTrue("Property was copied incorrectly!", process.getTemplates().contains(property));
    }

    @Test
    public void shouldCopyPropertyForWorkpiece() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle(NEW);
        property.setValue("workpiece");

        ProcessGenerator.copyPropertyForWorkpiece(process, property);
        assertTrue("Property was copied incorrectly!", process.getWorkpieces().contains(property));
    }

    @Test
    public void shouldNotCopyPropertyForProcess() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property newProperty = new Property();
        newProperty.setTitle("Process Property");
        newProperty.setValue("new value");

        boolean propertyForUpdateAlreadyExists = process.getProperties().stream()
                .anyMatch(property -> property.getTitle().equals(newProperty.getTitle())
                        && !property.getValue().equals(newProperty.getValue()));
        assertTrue("Property which is going be updated doesn't exists or have exactly the same value!",
            propertyForUpdateAlreadyExists);

        ProcessGenerator.copyPropertyForProcess(process, newProperty);

        boolean propertyValueUpdated = process.getProperties().stream()
                .anyMatch(property -> property.getTitle().equals(newProperty.getTitle())
                        && property.getValue().equals(newProperty.getValue()));
        assertTrue("Property value was not updated!", propertyValueUpdated);

    }

    @Test
    public void shouldNotCopyPropertyForTemplate() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property newProperty = new Property();
        newProperty.setTitle("template");
        newProperty.setValue("new value");

        boolean propertyForUpdateAlreadyExists = process.getTemplates().stream()
                .anyMatch(property -> property.getTitle().equals(newProperty.getTitle())
                        && !property.getValue().equals(newProperty.getValue()));
        assertTrue("Property which is going be updated doesn't exists or have exactly the same value!",
                propertyForUpdateAlreadyExists);

        ProcessGenerator.copyPropertyForTemplate(process, newProperty);

        boolean propertyValueUpdated = process.getTemplates().stream()
                .anyMatch(property -> property.getTitle().equals(newProperty.getTitle())
                        && property.getValue().equals(newProperty.getValue()));
        assertTrue("Property value was not updated!", propertyValueUpdated);
    }

    @Test
    public void shouldNotCopyPropertyForWorkpiece() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property newProperty = new Property();
        newProperty.setTitle("workpiece");
        newProperty.setValue("new value");

        boolean propertyForUpdateAlreadyExists = process.getWorkpieces().stream()
                .anyMatch(property -> property.getTitle().equals(newProperty.getTitle())
                        && !property.getValue().equals(newProperty.getValue()));
        assertTrue("Property which is going be updated doesn't exists or have exactly the same value!",
                propertyForUpdateAlreadyExists);

        ProcessGenerator.copyPropertyForWorkpiece(process, newProperty);

        boolean propertyValueUpdated = process.getWorkpieces().stream()
                .anyMatch(property -> property.getTitle().equals(newProperty.getTitle())
                        && property.getValue().equals(newProperty.getValue()));
        assertTrue("Property value was not updated!", propertyValueUpdated);
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
        assertEquals("Tasks were copied incorrectly!", 5, actual);
    }
}
