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

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.forms.CurrentTaskForm;
import org.kitodo.production.services.ServiceManager;

/**
 * An Active MQ service interface to create new processes.
 */
public class CreateNewProcessesProcessor extends ActiveMQProcessor {

    /**
     * The default constructor looks up the queue name to use in
     * kitodo_config.properties. If that is not configured and “null” is passed
     * to the super constructor, this will prevent
     * ActiveMQDirector.registerListeners() from starting this service.
     */
    public CreateNewProcessesProcessor() {
        super(ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_CREATE_NEW_PROCESSES_QUEUE).orElse(null));
    }

    /*
     * The main routine processing incoming tickets.
     */
    @Override
    protected void process(MapMessageObjectReader ticket) throws ProcessorException, JMSException {
        try {
            Integer projectId = ticket.getMandatoryInteger("project");
            Integer templateId = ticket.getMandatoryInteger("template");
            List<?> imports = ticket.getList("import"); // may be null
            String title = ticket.getString("title"); // may be null
            Integer parent = ticket.getInteger("parent"); // may be null
            Map<String, ?> metadata = ticket.getMapOfString("metadata"); // may
                                                                         // be
                                                                         // null
            
//            Integer stepID = ticket.getMandatoryInteger("id");
//            dialog.setCurrentTask(ServiceManager.getTaskService().getById(stepID));
//
//            if (ticket.hasField("properties")) {
//                updateProperties(dialog, ticket.getMapOfStringToString("properties"));
//            }
//            if (ticket.hasField("message")) {
//                Comment comment = new Comment();
//                comment.setProcess(dialog.getCurrentTask().getProcess());
//                comment.setMessage(ticket.getString("message"));
//                comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
//                comment.setType(CommentType.INFO);
//                comment.setCreationDate(new Date());
//                ServiceManager.getCommentService().saveToDatabase(comment);
//            }
//            dialog.closeTaskByUser();
        } catch (DAOException e) {
            throw new ProcessorException(e);
        }
    }

    /**
     * Transfers the properties to set into Production’s data model.
     *
     * @param dialog
     *            The CurrentTaskForm that we work with
     * @param propertiesToSet
     *            A Map with the properties to set
     */
    private void updateProperties(CurrentTaskForm dialog, Map<String, String> propertiesToSet) {
        List<Property> availableProperties = dialog.getProperties();
        for (Property property : availableProperties) {
            String key = property.getTitle();
            if (propertiesToSet.containsKey(key)) {
                String desiredValue = propertiesToSet.get(key);
                property.setValue(desiredValue);
                dialog.setProperty(property);
                dialog.saveCurrentProperty();
            }
        }
    }
}
