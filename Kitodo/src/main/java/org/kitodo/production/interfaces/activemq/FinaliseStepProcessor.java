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
import org.kitodo.production.forms.CurrentTaskForm;
import org.kitodo.production.services.ServiceManager;

/**
 * This is a web service interface to close steps. You have to provide the step
 * id as “id”; you can add a field “message” which will be added to the wiki
 * field.
 */
public class FinaliseStepProcessor extends ActiveMQProcessor {

    /**
     * The default constructor looks up the queue name to use in
     * kitodo_config.properties. If that is not configured and “null” is passed
     * to the super constructor, this will prevent
     * ActiveMQDirector.registerListeners() from starting this service.
     */
    public FinaliseStepProcessor() {
        super(ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_FINALIZE_STEP_QUEUE).orElse(null));
    }

    /**
     * This is the main routine processing incoming tickets. It gets an
     * CurrentTaskForm object, sets it to the appropriate step which is
     * retrieved from the database, appends the message − if any − to the wiki
     * field, and executes the form’s the step close function.
     *
     * @param ticket
     *            the incoming message
     *
     * @see org.kitodo.production.interfaces.activemq.ActiveMQProcessor#process(org.kitodo.production.interfaces.activemq.MapMessageObjectReader)
     */
    @Override
    protected void process(MapMessageObjectReader ticket) throws DAOException, JMSException {
        CurrentTaskForm dialog = new CurrentTaskForm();
        Integer stepID = ticket.getMandatoryInteger("id");
        dialog.setCurrentTask(ServiceManager.getTaskService().getById(stepID));
        if (ticket.hasField("properties")) {
            updateProperties(dialog, ticket.getMapOfStringToString("properties"));
        }
        if (ticket.hasField("message")) {
            Comment comment = new Comment();
            comment.setProcess(dialog.getCurrentTask().getProcess());
            comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
            comment.setMessage(ticket.getString("message"));
            comment.setType(CommentType.INFO);
            comment.setCreationDate(new Date());
            ServiceManager.getCommentService().saveToDatabase(comment);
        }
        dialog.closeTaskByUser();
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
