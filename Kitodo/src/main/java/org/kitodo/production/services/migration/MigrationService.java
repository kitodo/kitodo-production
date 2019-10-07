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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.migration.TaskComparator;
import org.kitodo.production.migration.TemplateComparator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MigrationService {

    private static final Logger logger = LogManager.getLogger(MigrationService.class);
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

    public void migrateMetadata() throws DAOException {
        List<Process> processes = ServiceManager.getProcessService().getAll();
        FileService fileService = ServiceManager.getFileService();
        URI metadataFilePath;
        for (Process process : processes) {
            try {
                metadataFilePath = fileService.getMetadataFilePath(process, true, true);
                ServiceManager.getDataEditorService().readData(metadataFilePath);
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
    }

    public boolean tasksAreEqual(List<Task> firstProcessTasks, List<Task> secondProcessTasks) {
        TaskComparator taskComparator = new TaskComparator();

        Iterator<Task> firstTaskIterator = firstProcessTasks.iterator();
        Iterator<Task> secondTaskIterator = secondProcessTasks.iterator();
        while (firstTaskIterator.hasNext() && secondTaskIterator.hasNext()) {
            Task firstTask = firstTaskIterator.next();
            Task secondTask = secondTaskIterator.next();
            if (taskComparator.compare(firstTask, secondTask) != 0) {
                return false;
            }
        }
        return true;
    }

    public String createTaskString(List<Task> processTasks) {
        String taskString = "";
        for (Task processTask : processTasks) {
            taskString = taskString.concat(processTask.getTitle());
        }
        return taskString.replaceAll("\\s", "");
    }

    public Map<Template, List<Process>> createTemplatesForProcesses(List<Process> processes, Workflow workflowToUse) {
        Map<Template, List<Process>> newTemplates = new HashMap<>();
        for (Process process : processes) {
            if (!templateListContainsTemplate(newTemplates, process, workflowToUse)) {
                Template template = new Template();
                template.setDocket(process.getDocket());
                template.setRuleset(process.getRuleset());
                template.setWorkflow(workflowToUse);
                template.setClient(process.getProject().getClient());
                newTemplates.put(template, new ArrayList<>(Arrays.asList(process)));
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

    public Map<Template, Template> getMatchingTemplates(Set<Template> templates) {
        Map<Template, Template> matchingTemplates = new HashMap<>();
        TemplateComparator templateComparator = new TemplateComparator();
        List<Template> existingTemplates = ServiceManager.getTemplateService().getAll();

        for (Template templateToCreate : templates) {
            for (Template existingTemplate : existingTemplates) {
                if (templateComparator.compare(templateToCreate, existingTemplate) == 0) {
                    matchingTemplates.put(templateToCreate, existingTemplate);
                }
            }
        }
        return matchingTemplates;
    }

    public void addProcessesToTemplate(Template template, List<Process> processesToAddToTemplate) throws DataException {
        for (Process process : processesToAddToTemplate) {
            process.setTemplate(template);
            ServiceManager.getProcessService().save(process);
        }
        template.getProcesses().addAll(processesToAddToTemplate);
        ServiceManager.getTemplateService().save(template);
    }
}
