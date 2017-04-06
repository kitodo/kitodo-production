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

package org.kitodo.mq.processors;

import de.sub.kitodo.config.ConfigCore;
import de.sub.kitodo.forms.AktuelleSchritteForm;

import java.util.List;
import java.util.Map;

import org.kitodo.mq.ActiveMQProcessor;
import org.kitodo.mq.MapMessageObjectReader;
import org.kitodo.production.properties.AccessCondition;
import org.kitodo.production.properties.ProcessProperty;
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
     * @see ActiveMQProcessor#process(MapMessageObjectReader)
     */
    @Override
    protected void process(MapMessageObjectReader ticket) throws Exception {
        AktuelleSchritteForm dialog = new AktuelleSchritteForm();
        Integer stepID = ticket.getMandatoryInteger("id");
        dialog.setMySchritt(serviceManager.getTaskService().find(stepID));
        if (ticket.hasField("properties")) {
            updateProperties(dialog, ticket.getMapOfStringToString("properties"));
        }
        if (ticket.hasField("message")) {
            serviceManager.getProcessService().addToWikiField(ticket.getString("message"),
                    dialog.getMySchritt().getProcess());
        }
        dialog.SchrittDurchBenutzerAbschliessen();
    }

    /**
     * The method updateProperties() transfers the properties to set into
     * kitodo’s data model.
     *
     * @param dialog
     *            The AktuelleSchritteForm that we work with
     * @param propertiesToSet
     *            A Map with the properties to set
     */
    protected void updateProperties(AktuelleSchritteForm dialog, Map<String, String> propertiesToSet) {
        List<ProcessProperty> availableProperties = dialog.getProcessProperties();
        for (int position = 0; position < availableProperties.size(); position++) {
            ProcessProperty propertyAtPosition = availableProperties.get(position);
            String key = propertyAtPosition.getName();
            if (propertiesToSet.containsKey(key)) {
                String desiredValue = propertiesToSet.get(key);
                AccessCondition permissions = propertyAtPosition.getCurrentStepAccessCondition();
                if (AccessCondition.WRITE.equals(permissions) || AccessCondition.WRITEREQUIRED.equals(permissions)) {
                    propertyAtPosition.setValue(desiredValue);
                    if (dialog.getContainer() == null || dialog.getContainer() == 0) {
                        dialog.setProcessProperty(propertyAtPosition);
                    } else {
                        availableProperties.set(position, propertyAtPosition);
                    }
                    dialog.saveCurrentProperty();
                }
            }
        }
    }
}
