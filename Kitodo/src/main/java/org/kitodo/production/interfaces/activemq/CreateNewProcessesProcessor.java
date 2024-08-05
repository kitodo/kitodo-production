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

import java.util.Collections;
import java.util.LinkedList;

import javax.jms.JMSException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.process.ProcessGenerator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;

/**
 * An Active MQ service interface to create new processes.
 */
public class CreateNewProcessesProcessor extends ActiveMQProcessor {
    private static final Logger logger = LogManager.getLogger(CreateNewProcessesProcessor.class);
    private final ProcessService processService = ServiceManager.getProcessService();

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

            // init
            ProcessGenerator processGenerator = new ProcessGenerator();
            boolean generated = processGenerator.generateProcess(order.getTemplateId(), order.getProjectId());
            if (!generated) {
                throw new ProcessGenerationException("Process was not generated");
            }
            Workpiece workpiece = new Workpiece();
            TempProcess tp = new TempProcess(processGenerator.getGeneratedProcess(), workpiece);

            if (order.getImports().isEmpty()) {
                // create process without import
            } else {
                // create process with import
            }
        } catch (DAOException /* | DataException */ | ProcessGenerationException e) {
            throw new ProcessorException(e.getMessage());
        }
    }
}
