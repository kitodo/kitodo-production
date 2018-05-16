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

package org.goobi.mq.processors;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.forms.AktuelleSchritteForm;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.goobi.mq.ActiveMQProcessor;
import org.goobi.mq.MapMessageObjectReader;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

/**
 * This is a web service interface to close steps. You have to provide the step
 * id as “id”; you can add a field “message” which will be added to the wiki
 * field.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class FinaliseStepProcessor extends ActiveMQProcessor {
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * The default constructor looks up the queue name to use in
     * kitodo_config.properties. If that is not configured and “null” is passed
     * to the super constructor, this will prevent
     * ActiveMQDirector.registerListeners() from starting this service.
     */
    public FinaliseStepProcessor() {
        super(ConfigCore.getParameter("activeMQ.finaliseStep.queue", null));
    }

    /**
     * This is the main routine processing incoming tickets. It gets an
     * AktuelleSchritteForm object, sets it to the appropriate step which is
     * retrieved from the database, appends the message − if any − to the wiki
     * field, and executes the form’s the step close function.
     *
     * @param ticket
     *            the incoming message
     *
     * @see org.goobi.mq.ActiveMQProcessor#process(org.goobi.mq.MapMessageObjectReader)
     */
    @Override
    protected void process(MapMessageObjectReader ticket) throws DAOException, DataException, IOException, JMSException {
        AktuelleSchritteForm dialog = new AktuelleSchritteForm();
        Integer stepID = ticket.getMandatoryInteger("id");
        dialog.setMySchritt(serviceManager.getTaskService().getById(stepID));
        if (ticket.hasField("properties")) {
            updateProperties(dialog, ticket.getMapOfStringToString("properties"));
        }
        if (ticket.hasField("message")) {
            serviceManager.getProcessService().addToWikiField(ticket.getString("message"),
                    dialog.getMySchritt().getProcess());
        }
        dialog.schrittDurchBenutzerAbschliessen();
    }

    /**
     * The method updateProperties() transfers the properties to set into
     * Goobi’s data model.
     *
     * @param dialog
     *            The AktuelleSchritteForm that we work with
     * @param propertiesToSet
     *            A Map with the properties to set
     */
    private void updateProperties(AktuelleSchritteForm dialog, Map<String, String> propertiesToSet) {
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
