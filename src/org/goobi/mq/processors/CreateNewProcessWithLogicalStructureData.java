/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.mq.processors;

import de.sub.goobi.config.ConfigMain;
import org.apache.log4j.Logger;
import org.goobi.mq.ActiveMQProcessor;
import org.goobi.mq.MapMessageObjectReader;

public class CreateNewProcessBasedOnEad extends ActiveMQProcessor {
    private static final Logger logger = Logger.getLogger(CreateNewProcessBasedOnEad.class);

    public CreateNewProcessBasedOnEad() {
        super(ConfigMain.getParameter("activeMQ.createNewProcessBasedOnEad.queue", null));
    }

    @Override
    protected void process(MapMessageObjectReader args) throws Exception {
        logger.info("CreateNewProcessBasedOnEad got new ticket.");

    }
}
