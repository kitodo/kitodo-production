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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jms.JMSException;

import org.apache.commons.lang3.ArrayUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.MediaNotFoundException;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.KitodoScriptService;
import org.kitodo.production.services.data.ProcessService;

/**
 * Executes instructions to start a Kitodo Script command from the Active MQ
 * interface. The MapMessage must contain the command statement in the
 * {@code script} argument. You pass a list of the process IDs as
 * {@code processes}.
 */
public class KitodoScriptProcessor extends ActiveMQProcessor {

    private final KitodoScriptService kitodoScriptService = ServiceManager.getKitodoScriptService();
    private final ProcessService processService = ServiceManager.getProcessService();

    public KitodoScriptProcessor() {
        super(ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_KITODO_SCRIPT_QUEUE).orElse(null));
    }

    @Override
    protected void process(MapMessageObjectReader ticket) throws ProcessorException, JMSException {
        final String[] allowedCommands = ConfigCore.getStringArrayParameter(
            ParameterCore.ACTIVE_MQ_KITODO_SCRIPT_ALLOW);
        try {
            String script = ticket.getMandatoryString("script");
            int space = script.indexOf(' ');
            if (!ArrayUtils.contains(allowedCommands, script.substring(7, space >= 0 ? space : script.length()))) {
                throw new IllegalArgumentException((space >= 0 ? script.substring(0, space) : script)
                        + " is not allowed");
            }
            Collection<Integer> processIds = ticket.getCollectionOfInteger("processes");
            List<Process> processes = new ArrayList<>(processIds.size());
            for (Integer id : processIds) {
                processes.add(processService.getById(id));
            }
            kitodoScriptService.execute(processes, script);
        } catch (DAOException | DataException | IOException | InvalidImagesException | MediaNotFoundException e) {
            throw new ProcessorException(e);
        }
    }
}
