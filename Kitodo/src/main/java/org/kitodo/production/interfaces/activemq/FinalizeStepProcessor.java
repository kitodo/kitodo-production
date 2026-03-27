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

package org.kitodo.production.interfaces.activemq;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.xml.sax.SAXException;

/**
 * This is a web service interface to close steps. You have to provide the step
 * id as “id”; you can add a field “message” which will be added to the process
 * comments.
 */
public class FinalizeStepProcessor extends ActiveMQProcessor {

    private final WorkflowControllerService workflowControllerService = new WorkflowControllerService();

    /**
     * The default constructor looks up the queue name to use in
     * kitodo_config.properties. If that is not configured and “null” is passed
     * to the super constructor, this will prevent
     * ActiveMQDirector.registerListeners() from starting this service.
     */
    public FinalizeStepProcessor() {
        super(ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_FINALIZE_STEP_QUEUE).orElse(null));
    }

    /**
     * This is the main routine processing incoming tickets. It loads the task for the appropriate step 
     * which is retrieved from the database, appends the message − if any − to process comments, and
     * closes the task.
     *
     * @param ticket
     *         the incoming message
     */
    @Override
    protected void process(MapMessageObjectReader ticket) throws ProcessorException, JMSException {
        try {
            Integer stepID = ticket.getMandatoryInteger("id");
            Task task = ServiceManager.getTaskService().getById(stepID);

            if (ticket.hasField("properties")) {
                updateProperties(task, ticket.getMapOfStringToString("properties"));
            }
            if (ticket.hasField("message")) {
                Comment comment = new Comment();
                comment.setProcess(task.getProcess());
                comment.setMessage(ticket.getString("message"));
                comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
                comment.setType(CommentType.INFO);
                comment.setCreationDate(new Date());
                ServiceManager.getCommentService().save(comment);
            }
            this.workflowControllerService.closeTaskByUser(task);
        } catch (DAOException | IOException | SAXException | FileStructureValidationException e) {
            throw new ProcessorException(e);
        }
    }

    /**
     * Transfers the properties to set into Production’s data model.
     *
     * @param task
     *            the task
     * @param propertiesToSet
     *            A Map with the properties to set
     */
    private void updateProperties(Task task, Map<String, String> propertiesToSet) throws ProcessorException {
        try {
            List<Property> availableProperties = task.getProcess().getProperties();
            for (Property property : availableProperties) {
                String key = property.getTitle();
                if (propertiesToSet.containsKey(key)) {
                    String desiredValue = propertiesToSet.get(key);
                    property.setValue(desiredValue);
                    ServiceManager.getPropertyService().save(property);
                }
            }
            ServiceManager.getProcessService().save(task.getProcess());
        } catch (DAOException e) {
            throw new ProcessorException(e);
        }
    }
}
