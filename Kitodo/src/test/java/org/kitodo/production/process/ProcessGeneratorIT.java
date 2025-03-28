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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGenerateProcess() throws Exception {
        ProcessGenerator processGenerator = new ProcessGenerator();
        boolean generated = processGenerator.generateProcess(5, 1);
        assertTrue(generated, "Process was not generated!");

        Process process = processGenerator.getGeneratedProcess();
        Template template = processGenerator.getTemplate();

        Ruleset expectedRuleset = template.getRuleset();
        Ruleset actualRuleset = process.getRuleset();
        assertEquals(expectedRuleset, actualRuleset, "Ruleset was copied incorrectly!");

        int expected = template.getTasks().size();
        int actual = process.getTasks().size();
        assertEquals(expected, actual, "Tasks were copied incorrectly!");
    }

    @Test
    public void shouldNotGenerateProcess() throws Exception {
        ProcessGenerator processGenerator = new ProcessGenerator();

        Exception exception = assertThrows(ProcessGenerationException.class, () -> processGenerator.generateProcess(2, 1));
        assertEquals("No steps of the workflow defined.", exception.getMessage());
    }

    @Test
    public void shouldAddPropertyForProcess() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);

        ProcessGenerator.addPropertyForProcess(process, NEW, "process");
        assertTrue(process.getProperties().stream().anyMatch(property -> property.getTitle().equals("new")), "Property was added incorrectly!");
    }

    @Test
    public void shouldAddPropertyForTemplate() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);

        ProcessGenerator.addPropertyForTemplate(process, NEW, "template");
        assertTrue(process.getTemplates().stream().anyMatch(property -> property.getTitle().equals(NEW)), "Property was added incorrectly!");
    }

    @Test
    public void shouldAddPropertyForWorkpiece() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);

        ProcessGenerator.addPropertyForWorkpiece(process, NEW, "workpiece");
        assertTrue(process.getWorkpieces().stream().anyMatch(property -> property.getTitle().equals(NEW)), "Property was added incorrectly!");
    }

    @Test
    public void shouldCopyPropertyForProcess() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle(NEW);
        property.setValue("process");

        ProcessGenerator.copyPropertyForProcess(process, new Property());
        assertTrue(process.getProperties().contains(property), "Property was copied incorrectly!");
    }

    @Test
    public void shouldCopyPropertyForTemplate() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle(NEW);
        property.setValue("template");

        ProcessGenerator.copyPropertyForTemplate(process, new Property());
        assertTrue(process.getTemplates().contains(property), "Property was copied incorrectly!");
    }

    @Test
    public void shouldCopyPropertyForWorkpiece() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle(NEW);
        property.setValue("workpiece");

        ProcessGenerator.copyPropertyForWorkpiece(process, property);
        assertTrue(process.getWorkpieces().contains(property), "Property was copied incorrectly!");
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
        assertTrue(propertyForUpdateAlreadyExists, "Property which is going be updated doesn't exists or have exactly the same value!");

        ProcessGenerator.copyPropertyForProcess(process, newProperty);

        boolean propertyValueUpdated = process.getProperties().stream()
                .anyMatch(property -> property.getTitle().equals(newProperty.getTitle())
                        && property.getValue().equals(newProperty.getValue()));
        assertTrue(propertyValueUpdated, "Property value was not updated!");

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
        assertTrue(propertyForUpdateAlreadyExists, "Property which is going be updated doesn't exists or have exactly the same value!");

        ProcessGenerator.copyPropertyForTemplate(process, newProperty);

        boolean propertyValueUpdated = process.getTemplates().stream()
                .anyMatch(property -> property.getTitle().equals(newProperty.getTitle())
                        && property.getValue().equals(newProperty.getValue()));
        assertTrue(propertyValueUpdated, "Property value was not updated!");
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
        assertTrue(propertyForUpdateAlreadyExists, "Property which is going be updated doesn't exists or have exactly the same value!");

        ProcessGenerator.copyPropertyForWorkpiece(process, newProperty);

        boolean propertyValueUpdated = process.getWorkpieces().stream()
                .anyMatch(property -> property.getTitle().equals(newProperty.getTitle())
                        && property.getValue().equals(newProperty.getValue()));
        assertTrue(propertyValueUpdated, "Property value was not updated!");
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
        assertEquals(5, actual, "Tasks were copied incorrectly!");
    }
}
