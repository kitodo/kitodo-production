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

import javax.jms.JMSException;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ProcessorException;

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
            CreateNewProcessOrder order = new CreateNewProcessOrder(ticket);


        } catch (IllegalArgumentException e) {
            // ticket.getMandatory... value not found (null)
            throw new ProcessorException(e);
        } catch (DAOException e) {
            // importconfiguration not found
            throw new ProcessorException(e);
        }
    }
}
