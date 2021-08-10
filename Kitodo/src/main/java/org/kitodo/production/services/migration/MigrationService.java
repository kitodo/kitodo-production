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

package org.kitodo.production.services.migration;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.migration.TaskComparer;
import org.kitodo.production.migration.TemplateComparer;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class MigrationService {

    private static final Logger logger = LogManager.getLogger(MigrationService.class);
    public static final char SEPARATOR = 0x1F;
    private static volatile MigrationService instance = null;

    /**
     * Return singleton variable of type MigrationService.
     *
     * @return unique instance of MigrationService
     */
    public static MigrationService getInstance() {
        MigrationService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (MigrationService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new MigrationService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Migrates the meta.xml to the new Kitodo format.
     *
     * @param process
     *            process whose meta.xml is to be migrated
     * @throws DAOException
     *             when Database access fails
     */
    public void migrateMetadata(Process process) throws DAOException {
        FileService fileService = ServiceManager.getFileService();
        URI metadataFilePath;
        try {
            metadataFilePath = fileService.getMetadataFilePath(process, true, true);
            ServiceManager.getDataEditorService().readData(metadataFilePath);
            Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
            workpiece.setId(process.getId().toString());
            ServiceManager.getMetsService().saveWorkpiece(workpiece, metadataFilePath);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Test if a list of processes is equal, concerning the TaskComparator.
     * @param firstProcessTasks The first list of tasks
     * @param secondProcessTasks the second list of tasks
     * @return true, if the lists are equal, false otherwise.
     */
    public boolean tasksAreEqual(List<Task> firstProcessTasks, List<Task> secondProcessTasks) {
        TaskComparer taskComparer = new TaskComparer();

        Iterator<Task> firstTaskIterator = firstProcessTasks.iterator();
        Iterator<Task> secondTaskIterator = secondProcessTasks.iterator();
        while (firstTaskIterator.hasNext() && secondTaskIterator.hasNext()) {
            Task firstTask = firstTaskIterator.next();
            Task secondTask = secondTaskIterator.next();
            if (!taskComparer.isEqual(firstTask, secondTask)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a String out of the tasks to identify different tasks orders.
     * @param processTasks The List of tasks to generate a string from.
     * @return A string identifying the tasks.
     */
    public String createTaskString(List<Task> processTasks) {
        processTasks.sort(Comparator.comparingInt(Task::getOrdering));
        String taskString = processTasks.stream().map(Task::getTitle).collect(Collectors.joining(", "));
        String hashCode = Integer.toHexString(processTasks.parallelStream().mapToInt(TaskComparer::hashCode).sum());
        return taskString + SEPARATOR + hashCode;
    }

    /**
     * Creates templates for a list of processes with a given workflow.
     * @param processes The list of processes to create the templates for.
     * @param workflowToUse the workflow to use for the template.
     * @return A map with templates and the corresponding processes.
     */
    public Map<Template, List<Process>> createTemplatesForProcesses(List<Process> processes, Workflow workflowToUse) {
        Map<Template, List<Process>> newTemplates = new HashMap<>();
        for (Process process : processes) {
            if (!templateListContainsTemplate(newTemplates, process, workflowToUse)) {
                Template template = new Template();
                template.setDocket(process.getDocket());
                template.setRuleset(process.getRuleset());
                template.setWorkflow(workflowToUse);
                template.setClient(process.getProject().getClient());
                template.setProjects(new ArrayList<>(Collections.singletonList(process.getProject())));
                newTemplates.put(template, new ArrayList<>(Collections.singletonList(process)));
            }
        }
        return newTemplates;
    }

    private boolean templateListContainsTemplate(Map<Template, List<Process>> newTemplates, Process process, Workflow workflowToUse) {
        for (Template template : newTemplates.keySet()) {
            if (template.getRuleset().equals(process.getRuleset()) && template.getDocket().equals(process.getDocket())
                    && template.getWorkflow().equals(workflowToUse)) {
                newTemplates.get(template).add(process);
                return true;
            }
        }

        return false;
    }

    /**
     * Matches templates from the database to a list of given templates.
     * @param templates the templates to find matching templates in the database for.
     * @return A Map with matching templates
     * @throws DAOException is thrown if the database access fails
     */
    public Map<Template, Template> getMatchingTemplates(Set<Template> templates) throws DAOException {
        Map<Template, Template> matchingTemplates = new HashMap<>();
        TemplateComparer templateComparer = new TemplateComparer();
        List<Template> existingTemplates = ServiceManager.getTemplateService().getAll();

        for (Template templateToCreate : templates) {
            for (Template existingTemplate : existingTemplates) {
                if (templateComparer.isEqual(templateToCreate, existingTemplate)) {
                    matchingTemplates.put(templateToCreate, existingTemplate);
                }
            }
        }
        return matchingTemplates;
    }

    /**
     * Adds a list of processes to a given template.
     * @param template The template to add the processes to.
     * @param processesToAddToTemplate the processes to be added to the template
     * @throws DataException is thrown if database access fails
     */
    public void addProcessesToTemplate(Template template, List<Process> processesToAddToTemplate) throws DataException {
        int numberOfProcesses = processesToAddToTemplate.size();
        HashMap<Project,String> projects = new HashMap<>();
        for (int currentProcess = 0; currentProcess < numberOfProcesses; currentProcess++) {
            Process process = processesToAddToTemplate.get(currentProcess);
            logger.trace("Assigning template \"{}\" to process \"{}\" ({}% complete)", template.getTitle(),
                process.getTitle(), 100 * currentProcess / numberOfProcesses);
            process.setTemplate(template);
            projects.put(process.getProject(),"");
            ServiceManager.getProcessService().save(process, true);
        }
        template.getProjects().addAll(projects.keySet());
        ServiceManager.getTemplateService().save(template);
    }

    /**
     * Checks if the template tile is already in use or is empty.
     * @param template the template to check
     * @return true, is title is valid, false if title is empty or already in use
     */
    public boolean isTitleValid(Template template) {
        String templateTitle = template.getTitle();
        if (StringUtils.isNotBlank(templateTitle)) {
            List<Template> templates = ServiceManager.getTemplateService().getTemplatesWithTitleAndClient(templateTitle,
                template.getClient().getId());
            int count = templates.size();
            if (count != 0) {
                Helper.setErrorMessage("templateTitleAlreadyInUse");
                return false;
            }
            return true;
        }
        Helper.setErrorMessage("templateTitleEmpty");
        return false;
    }
}
