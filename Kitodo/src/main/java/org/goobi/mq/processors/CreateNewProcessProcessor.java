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

import org.goobi.mq.ActiveMQProcessor;
import org.goobi.mq.MapMessageObjectReader;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;

/**
 * CreateNewProcessProcessor is an Apache Active MQ consumer which registers to
 * a queue configured by "activeMQ.createNewProcess.queue" on application
 * startup. It was designed to create new processes from outside Production. There
 * are two ways providing to create new processes. If the MapMessage on that
 * queue contains of all the fields listed, the bibliographic data is retrieved
 * using a catalog configured within Production. If “opac” is missing, it will try
 * to create a process just upon the data passed in the “userFields” − “field”
 * and “value” will be ignored in that case, and the “docType” can be set
 * manually.
 *
 * <p>
 * Field summary:
 *
 * <dl>
 * <dt>String template</dt>
 * <dd>name of the process template to use.</dd>
 * <dt>String opac</dt>
 * <dd>Cataloge to use for lookup.</dd>
 * <dt>String field</dt>
 * <dd>Field to look into, usually 12 (PPN).</dd>
 * <dt>String value</dt>
 * <dd>Value to look for, id of physical medium</dd>
 * <dt>String docType</dt>
 * <dd>DocType value to use if no catalog request is performed.</dd>
 * <dt>Set&lt;String&gt; collections</dt>
 * <dd>Collections to be selected.</dd>
 * <dt>Map&lt;String, String&gt; userFields collections</dt>
 * <dd>Fields to be populated manually.</dd>
 * </dl>
 */
public class CreateNewProcessProcessor extends ActiveMQProcessor {

    public CreateNewProcessProcessor() {
        super(ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_CREATE_NEW_PROCESSES_QUEUE).orElse(null));
    }

    @Override
    protected void process(MapMessageObjectReader args) {
      throw new UnsupportedOperationException();
    }

}
